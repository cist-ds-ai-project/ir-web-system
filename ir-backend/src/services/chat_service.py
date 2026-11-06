import os

import httpx

SYSTEM_PROMPT = """
あなたはシステム組み込み用AIです。
必ず日本語で簡潔に答えてください。
"""

OLLAMA_URL = os.environ.get("OLLAMA_URL", "http://localhost:11434")
MODEL = "qwen2.5:7b"


async def chat(message: str) -> str:
    """Ollama APIにチャットリクエストを送信し、応答テキストを返す"""
    async with httpx.AsyncClient() as client:
        response = await client.post(
            f"{OLLAMA_URL}/api/chat",
            json={
                "model": MODEL,
                "messages": [
                    {"role": "system", "content": SYSTEM_PROMPT},
                    {"role": "user", "content": message},
                ],
                "stream": False,
            },
            timeout=120.0,
        )
        response.raise_for_status()
        data = response.json()
        return data["message"]["content"]
