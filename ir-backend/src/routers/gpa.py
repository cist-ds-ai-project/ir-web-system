from fastapi import APIRouter

from src.models.gpa import (
    GpaSelectionOptionsResponse,
    GpaRawResponse,
    DepartmentGpaResponse,
    GpaStatsResponse,
    GpaFilter
)
from src.services import gpa_service

router = APIRouter(prefix="/gpa", tags=["gpa"])

@router.get("/options", response_model=GpaSelectionOptionsResponse)
async def get_gpa_options():
    """GPAページ用の選択肢（年度・学部・学科・学年）"""
    return await gpa_service.get_selection_options()

@router.post("/raw", response_model=GpaRawResponse)
async def get_gpa_raw(filter: GpaFilter):
    """ヒストグラム用GPAデータ取得"""
    return await gpa_service.get_raw_gpa(filter)

@router.post("/by-department", response_model=DepartmentGpaResponse)
async def get_gpa_by_department(filter: GpaFilter):
    """学科ごとのGPA配列を取得"""
    return await gpa_service.get_gpa_by_department(filter)

@router.post("/stats", response_model=GpaStatsResponse)
async def get_gpa_stats(filter: GpaFilter):
    """平均・分散などのグリッド列内容取得"""
    return await gpa_service.get_stats(filter)
