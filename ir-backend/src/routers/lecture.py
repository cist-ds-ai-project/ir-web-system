from fastapi import APIRouter
from src.models.lecture import LectureListResponse, SelectionOptionsResponse
from src.services import lecture_service


router = APIRouter(prefix="/lecture", tags=["lecture"])

@router.get("/options", response_model=SelectionOptionsResponse)
async def get_lecture_options():
    """選択肢（学年、学科）を取得"""
    return await lecture_service.get_selection_options()

@router.get("/all", response_model=LectureListResponse)
async def get_all_lectures():
    """すべて科目の講義情報を取得"""
    records = await lecture_service.get_all_lectures()
    return LectureListResponse(lectures=records)