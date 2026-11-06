from fastapi import APIRouter

from src.models.grade import (
    GradeDetailResponse,
    CategoryListResponse,
)
from src.services import grade_service

router = APIRouter(prefix="/grade", tags=["grade"])



@router.get("/detail/{lecture_name}", response_model=GradeDetailResponse)
async def get_grade_detail(lecture_name: str):
    return await grade_service.get_lecture_distribution(lecture_name)


@router.get("/categories", response_model=CategoryListResponse)
async def get_categories():
    # サービスからは辞書のリスト（[{'value': 1, ...}, {...}]）が返ってくる
    items = await grade_service.get_categories()
    return CategoryListResponse(categories=items)

