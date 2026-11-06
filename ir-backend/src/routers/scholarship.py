from datetime import date
from fastapi import APIRouter, Depends, Query
from src.models.scholarship import ScholarshipDistributionResponse, ScholarshipTrendResponse
from src.services import scholarship
from typing import List, Optional

router = APIRouter(prefix="/scholarship", tags=["scholarship"])

@router.get("/all", response_model=ScholarshipDistributionResponse)
async def get_distribution(
    type: Optional[List[str]] = Query(None),
    academic_year: Optional[List[str]] = Query(None),
    faculty: Optional[List[str]] = Query(None),
    department: Optional[List[str]] = Query(None),
):
    type_param = type[0] if type else None
    academic_year_param = academic_year[0] if academic_year else None
    faculty_param = faculty[0] if faculty else None
    department_param = department[0] if department else None

    return await scholarship.get_scholarship_distribution(
        type=type_param,
        academic_year=academic_year_param,
        faculty=faculty_param,
        department=department_param
    )

@router.get("/trend", response_model=ScholarshipTrendResponse)
async def get_trend(
    type: Optional[List[str]] = Query(None),
    faculty: Optional[List[str]] = Query(None),
    department: Optional[List[str]] = Query(None),
):
    type_param = type[0] if type else None
    faculty_param = faculty[0] if faculty else None
    department_param = department[0] if department else None

    return await scholarship.get_scholarship_trend(
        type=type_param,
        faculty=faculty_param,
        department=department_param
    )

@router.get("/types", response_model=List[str])
async def get_scholarship_types_endpoint():
    return await scholarship.get_scholarship_types()

@router.get("/academic-years", response_model=List[str])
async def get_academic_years():
    return await scholarship.get_academic_years()

@router.get("/faculties", response_model=List[str])
async def get_faculties():
    return await scholarship.get_faculties()

@router.get("/departments", response_model=List[str])
async def get_departments(faculty: Optional[str] = Query(None)):
    return await scholarship.get_departments(faculty)

@router.get("/enrollment-date", response_model=List[str])
async def get_enrollment_date():
    return await scholarship.get_enrollment_date()