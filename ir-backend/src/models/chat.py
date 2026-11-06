from pydantic import BaseModel


class ChatRequest(BaseModel):
    """チャットAPIのリクエストモデル"""
    message: str


class ChatResponse(BaseModel):
    """チャットAPIのレスポンスモデル"""
    answer: str
