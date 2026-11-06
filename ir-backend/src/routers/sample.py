from fastapi import APIRouter
from src.models.sample import DepartmentGpaResponse
from src.services import sample_service

router = APIRouter()


@router.get("/sample/gpa-by-department", response_model=DepartmentGpaResponse)
async def gpa_by_department():
    """学科別の平均GPAを返すサンプルAPI"""
    records = await sample_service.get_department_avg_gpa()
    return DepartmentGpaResponse(data=records)
