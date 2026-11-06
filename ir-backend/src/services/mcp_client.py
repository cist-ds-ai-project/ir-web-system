import json  # JSON文字列をPythonの辞書に変換するために使います。
from typing import Any  # 関数の戻り値や引数に任意の型を指定するために使います。

import httpx  # DBHubのMCPエンドポイントへHTTP通信するために使います。


class MCPClient:  # DBHub MCPと通信するためのクライアントクラスです。
    def __init__(self, url: str = "http://127.0.0.1:8081/mcp") -> None:  # 接続先URLを受け取って初期化します。
        self.url = url  # DBHubのMCPエンドポイントURLを保存します。
        self.session_id: str | None = None  # MCPのセッションIDを保存します。
        self.request_id = 1  # JSON-RPCのリクエストIDを1から始めます。

    def _next_id(self) -> int:  # 次のJSON-RPC IDを作る関数です。
        current_id = self.request_id  # 現在のIDを変数に保存します。
        self.request_id += 1  # 次回用にIDを1増やします。
        return current_id  # 今回使うIDを返します。

    def _headers(self) -> dict[str, str]:  # MCP通信で使うHTTPヘッダーを作る関数です。
        headers = {  # 基本のヘッダーを辞書として作ります。
            "Content-Type": "application/json",  # 送信する本文がJSONであることを示します。
            "Accept": "application/json, text/event-stream",  # JSONまたはSSE形式の応答を受け取れることを示します。
        }
        if self.session_id is not None:  # すでにセッションIDを持っている場合だけ処理します。
            headers["Mcp-Session-Id"] = self.session_id  # MCPセッションIDをヘッダーに追加します。
        return headers  # 完成したヘッダーを返します。

    def _parse_response(self, response: httpx.Response) -> dict[str, Any] | None:  # HTTP応答をJSON-RPCの辞書に変換します。
        if "mcp-session-id" in response.headers:  # 応答ヘッダーにセッションIDがあるか確認します。
            self.session_id = response.headers["mcp-session-id"]  # セッションIDを保存します。

        if response.status_code == 202:  # 通知メッセージでは202だけ返る場合があります。
            return None  # 本文がないのでNoneを返します。

        response.raise_for_status()  # HTTPエラーがあれば例外を発生させます。

        content_type = response.headers.get("content-type", "")  # 応答のContent-Typeを取得します。
        text = response.text.strip()  # 応答本文の前後の空白を取り除きます。

        if "text/event-stream" in content_type:  # SSE形式で返ってきた場合の処理です。
            for line in text.splitlines():  # 応答を1行ずつ確認します。
                if line.startswith("data:"):  # data: で始まる行だけを対象にします。
                    data = line.removeprefix("data:").strip()  # data: を取り除いてJSON部分だけにします。
                    return json.loads(data)  # JSON文字列を辞書に変換して返します。
            return None  # data: 行が見つからなければNoneを返します。

        if text == "":  # 応答本文が空の場合の処理です。
            return None  # 空なのでNoneを返します。

        return response.json()  # 通常のJSON応答を辞書として返します。

    def _post(self, payload: dict[str, Any]) -> dict[str, Any] | None:  # DBHubへPOSTする共通関数です。
        response = httpx.post(  # HTTP POSTリクエストを送ります。
            self.url,  # 送信先はDBHubのMCPエンドポイントです。
            headers=self._headers(),  # MCP用のヘッダーを付けます。
            json=payload,  # JSON-RPCの本文を送ります。
            timeout=30.0,  # 最大30秒待ちます。
        )
        return self._parse_response(response)  # 応答を解析して返します。

    def initialize(self) -> dict[str, Any] | None:  # MCPの初期化を行う関数です。
        payload = {  # initialize用のJSON-RPC本文を作ります。
            "jsonrpc": "2.0",  # JSON-RPC 2.0を使うことを示します。
            "id": self._next_id(),  # リクエストIDを設定します。
            "method": "initialize",  # MCPの初期化メソッドを指定します。
            "params": {  # initializeに渡すパラメータです。
                "protocolVersion": "2025-06-18",  # 使用するMCPプロトコルバージョンです。
                "capabilities": {},  # 今回のクライアント側機能は空で指定します。
                "clientInfo": {  # クライアント情報を指定します。
                    "name": "ir-backend-mcp-client",  # クライアント名です。
                    "version": "0.1.0",  # クライアントのバージョンです。
                },
            },
        }
        result = self._post(payload)  # initializeリクエストを送信します。

        initialized_payload = {  # initialized通知用の本文を作ります。
            "jsonrpc": "2.0",  # JSON-RPC 2.0を使うことを示します。
            "method": "notifications/initialized",  # 初期化完了通知を送ります。
            "params": {},  # 通知に追加パラメータはありません。
        }
        self._post(initialized_payload)  # initialized通知を送信します。
        return result  # initializeの結果を返します。

    def list_tools(self) -> dict[str, Any] | None:  # DBHubが持つツール一覧を取得する関数です。
        payload = {  # tools/list用のJSON-RPC本文を作ります。
            "jsonrpc": "2.0",  # JSON-RPC 2.0を使うことを示します。
            "id": self._next_id(),  # リクエストIDを設定します。
            "method": "tools/list",  # ツール一覧取得メソッドを指定します。
            "params": {},  # 追加パラメータはありません。
        }
        return self._post(payload)  # DBHubへ送信して結果を返します。

    def execute_sql(self, sql: str) -> dict[str, Any] | None:  # SQLをDBHub経由で実行する関数です。
        payload = {  # tools/call用のJSON-RPC本文を作ります。
            "jsonrpc": "2.0",  # JSON-RPC 2.0を使うことを示します。
            "id": self._next_id(),  # リクエストIDを設定します。
            "method": "tools/call",  # ツール実行メソッドを指定します。
            "params": {  # 実行するツールの情報を指定します。
                "name": "execute_sql",  # DBHubのexecute_sqlツールを指定します。
                "arguments": {  # execute_sqlへ渡す引数です。
                    "sql": sql,  # 実行したいSQL文を渡します。
                },
            },
        }
        return self._post(payload)  # DBHubへ送信して結果を返します。


def main() -> None:  # このファイルを単体実行したときの確認処理です。
    client = MCPClient()  # DBHub用のクライアントを作ります。

    print("initialize を実行します。")  # 初期化開始を表示します。
    initialize_result = client.initialize()  # MCPのinitializeを実行します。
    print(json.dumps(initialize_result, ensure_ascii=False, indent=2))  # 結果を見やすいJSONで表示します。

    print("tools/list を実行します。")  # ツール一覧取得開始を表示します。
    tools_result = client.list_tools()  # DBHubのツール一覧を取得します。
    print(json.dumps(tools_result, ensure_ascii=False, indent=2))  # 結果を見やすいJSONで表示します。

    print("execute_sql でテーブル一覧を取得します。")  # SQL実行開始を表示します。
    sql_result = client.execute_sql("""
                                    SELECT table_schema, table_name
                                    FROM information_schema.tables
                                    WHERE table_schema IN ('data', 'public')
                                    ORDER BY table_schema, table_name;
                                    """)
    print(json.dumps(sql_result, ensure_ascii=False, indent=2))  # 結果を見やすいJSONで表示します。

if __name__ == "__main__":
     main()  # 動作確認用のmain関数を実行します。