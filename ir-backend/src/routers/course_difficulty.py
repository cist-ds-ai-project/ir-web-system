# routers/course_difficulty.py

from fastapi import APIRouter
from src.models.course_difficulty import AlertTargetListResponse
from src.services import course_difficulty_service

router = APIRouter(prefix="/course-difficulty", tags=["course_difficulty"])


@router.get("/alerts", response_model=AlertTargetListResponse)
async def get_alert_targets():
    """異常な成績分布を持つ科目一覧を取得する"""
    return await course_difficulty_service.detect_alert_targets()