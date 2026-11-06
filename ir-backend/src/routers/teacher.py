from fastapi import APIRouter

from src.services import teacher_service


router = APIRouter(
    prefix="/teacher",
    tags=["teacher"]
)


@router.get("/courses/{person_id}")
async def get_teacher_courses(person_id: str):
    return await teacher_service.get_teacher_courses(person_id)