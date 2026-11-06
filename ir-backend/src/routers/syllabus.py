from fastapi import APIRouter, HTTPException
from src.models.syllabus import SyllabusDetailResponse
from src.services import syllabus_service


router = APIRouter(prefix="/syllabus", tags=["syllabus"])


@router.get("/{course_id}", response_model=SyllabusDetailResponse)
async def get_syllabus_detail(course_id: str):
    """指定した科目のシラバス詳細を取得"""
    result = await syllabus_service.get_syllabus_detail(course_id)
    if result is None:
        raise HTTPException(status_code=404, detail="該当するシラバスが見つかりません")
    return SyllabusDetailResponse(**result)
