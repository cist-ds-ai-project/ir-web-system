"""neko上のapp.py由来のサンプル/疎通確認用チャットAPI"""

from fastapi import APIRouter

from src.models.chat import ChatRequest, ChatResponse
from src.services import chat_service

router = APIRouter(tags=["chat"])


@router.post("/chat", response_model=ChatResponse)
async def chat(req: ChatRequest):
    """ユーザーメッセージをOllamaに送信し、AI応答を返す"""
    answer = await chat_service.chat(req.message)
    return ChatResponse(answer=answer)
