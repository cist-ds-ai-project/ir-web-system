from fastapi import APIRouter
from src.models.enrollment import (
    CategoryListResponse,
    EnrollmentTrendResponse,
    LectureListResponse,
    SelectionOptionsResponse,
)
from src.services import Enrollment_service

router = APIRouter(
    prefix="/enrollment",
    tags=["enrollment"]
)


@router.get("/options", response_model=SelectionOptionsResponse)
async def get_enrollment_options():
    return await Enrollment_service.get_selection_options()


@router.get("/lectures", response_model=LectureListResponse)
async def get_all_lectures():
    records = await Enrollment_service.get_all_lectures()
    return LectureListResponse(lectures=records)


@router.get("/course/{course_id}", response_model=EnrollmentTrendResponse)
async def get_enrollment_by_course_id(course_id: str):
    return await Enrollment_service.get_enrollment_trend_by_course_id(course_id)


@router.get("/categories", response_model=CategoryListResponse)
async def get_categories():
    items = await Enrollment_service.get_categories()
    return CategoryListResponse(categories=items)


@router.get("/{lecture_name}", response_model=EnrollmentTrendResponse)
async def get_enrollment(lecture_name: str):
    return await Enrollment_service.get_enrollment_trend(
        lecture_name
    )
