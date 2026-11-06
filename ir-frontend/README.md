# ir-frontend

大学の教学データ分析システム（IR支援アプリケーション）のフロントエンドである。
本プロジェクトはSpring Boot と Vaadinをベースとした UI 構築を行うが、データの詳細な可視化が必要なグラフコンポーネントの作成については Reactを使用して実装を行う。

---

## 技術スタック

| カテゴリ | 技術 |
|---|---|
| フレームワーク | Vaadin 25 / Spring Boot 4 |
| 言語 | Java 25 / TypeScript (React) |
| ビルドツール | Maven / npm |
| DB アクセス | JdbcClient（SQL 直書き） |
| API 通信 | Spring HTTP Interface（@HttpExchange） |

---

## 前提条件

<!-- TODO: 必要なソフトウェアとバージョンを記載 -->

---

## 環境構築

開発環境の構築手順は、以下のPDFを参照すること。
（ただし、2024年時点の資料であるため更新が必要である。）
[setup-guide.pdf](./setup-guide.pdf)

---

## 開発・実行コマンド
Gitのブランチは、チームごとに作成する。
- チームのブランチ：<チーム名>/main
- 個人のブランチ：<チーム名>/<個人名>

---

## アーキテクチャ概要

### 3層アーキテクチャ

保守性と拡張性を高めるため、システムを役割ごとに「画面層」「業務ロジック層」「データアクセス層」の3つに分割して管理する三層アーキテクチャという設計パターンを採用する。

[3層アーキテクチャの解説 (docs/architecture.md)](./docs/architecture.md)

各層は上から下への一方向にのみ依存する。

```
┌─────────────────────────────┐
│  presentation (画面層)       │  ← ユーザーに見える画面を作る
│  View, React ラッパー        │
├─────────────────────────────┤
│  service (業務ロジック層)     │  ← データの加工・判定・計算を行う
│  Service                    │
├─────────────────────────────┤
│  repository (データアクセス層) │  ← 外部 API や DB とやり取りする
│  ApiClient / Repository     │
├─────────────────────────────┤
│  model (データ定義)           │  ← 扱うデータの構造を定義する
│    ├ entity                  │  ← DB から取得するデータの構造
│    └ dto                     │  ← Python API から受け取る JSON の構造
├─────────────────────────────┤
│  config (設定)               │  ← HTTP クライアント等の設定
└─────────────────────────────┘
```

### フォルダ構成

各層の中に機能名のサブパッケージを作り、関連クラスをまとめる。
複数機能で共有するクラスは `common/` に配置する。

```
ir-frontend/
└── src/
    ├── main/
    │   ├── java/jp/ac/ir/              ★ Java コードを書く場所
    │   │   ├── presentation/           … 画面層
    │   │   │   ├── common/             … 共通 UI パーツ (MainLayout 等)
    │   │   │   └── <機能名>/           … 機能ごとの View・React ラッパー
    │   │   ├── service/                … 業務ロジック層
    │   │   │   └── <機能名>/           … 機能ごとの Service
    │   │   ├── repository/             … データアクセス層
    │   │   │   └── <機能名>/           … 機能ごとの ApiClient / Repository
    │   │   ├── model/                  … データ定義
    │   │   │   ├── entity/<機能名>/    … DB から取得するデータの構造
    │   │   │   └── dto/<機能名>/       … Python API から受け取る JSON の構造
    │   │   └── config/                 … 設定クラス (HTTP クライアント等)
    │   ├── frontend/components/        ★ React コンポーネントを書く場所
    │   │   ├── common/                 … 共通コンポーネント
    │   │   └── <機能名>/               … 機能ごとのコンポーネント
    │   └── resources/                  ★ 設定ファイル・CSS を置く場所
    └── test/java/                      ★ テストコードを書く場所
```

> `<機能名>` はシンプルで機能が分かりやすい名前を使う。

### 各フォルダに置くファイルの役割

| 層 | フォルダ | 置くもの | 役割 |
|---|---|---|---|
| 画面 | `presentation/<機能名>/` | View, React ラッパー | ユーザーに表示する画面を組み立てる |
| 画面（共通） | `presentation/common/` | MainLayout, 共通コンポーネント | 複数機能で共有する UI パーツ |
| 業務ロジック | `service/<機能名>/` | Service | データの加工・検証・集計などのロジック |
| データアクセス | `repository/<機能名>/` | ApiClient | `@HttpExchange` で Python API を宣言的に呼び出す |
| データアクセス | `repository/<機能名>/` | Repository | JdbcClient を使い SQL を直接書いて DB を操作する |
| 設定 | `config/` | Config | HTTP クライアントや共通設定を管理する |
| データ定義 | `model/entity/<機能名>/` | record / クラス | DB から取得するデータの構造定義 |
| データ定義 | `model/dto/<機能名>/` | record / クラス | Python API から受け取る JSON の構造定義 |

### 命名規則

#### ファイル名

機能や層が分かりやすいよう、用途に応じた接尾辞（サフィックス）をつける。

| 層 | クラス名の接尾辞 | 例 |
|---|---|---|
| presentation | `〜View`, `〜Component`, `〜Chart` | `GpaAnalysisView.java` |
| service | `〜Service` | `GpaService.java` |
| repository (API) | `〜ApiClient`（interface） | `GpaApiClient.java` |
| repository (DB) | `〜Repository` | `GpaRepository.java` |
| model (dto/entity) | データ名そのもの | `DepartmentGpa.java` |
| config | `〜Config` | `HttpClientConfig.java` |

#### クラス・メソッド・変数の規則

| 対象 | 適用規則 | 例 |
|---|---|---|
| Java クラス / インターフェース | **PascalCase**（アッパーキャメル） | `GpaService`, `MainLayout` |
| Java メソッド / 変数 | **camelCase**（ローワーキャメル） | `getStudentList()`, `departmentName` |
| React コンポーネント (ファイル名・関数名) | **PascalCase**（アッパーキャメル） | `GpaChart.tsx`, `StudentGraph` |
| TypeScript / JS 関数・変数 | **camelCase**（ローワーキャメル） | `fetchData()`, `chartData` |

### 新機能を追加するとき

各層に同名のサブパッケージを作成する。

例：「GPAの推移閲覧機能」を追加する場合

1. `presentation/gpa/` に View を作成
2. `service/gpa/` に Service を作成
3. `repository/gpa/` に ApiClient または Repository を作成
4. `model/dto/gpa/` または `model/entity/gpa/` に データ定義を作成
5. `frontend/components/gpa/` に React コンポーネントを作成

---

## 環境設定

application.propertiesファイルにDB接続情報などを記述する。
