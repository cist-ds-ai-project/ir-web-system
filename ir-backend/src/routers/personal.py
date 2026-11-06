from fastapi import APIRouter
from src.services import personal_service

router = APIRouter(prefix="/personal", tags=["personal"])


@router.get("/grades/{person_id}")
async def get_personal_grades(person_id: str):
    """ログイン中ユーザーの個人成績を取得するAPI"""
    return await personal_service.get_personal_grades(person_id)

@router.get("/options")
async def get_personal_options():
    return await personal_service.get_selection_options()

@router.get("/gpa/{person_id}")
async def get_personal_gpa(person_id: str):
    """ログイン中ユーザーの年度・学期ごとのGPAを取得するAPI"""
    return await personal_service.get_gpa_by_year_semester(person_id)

@router.get("/questionnaire/{person_id}/{course_id}")
async def get_personal_questionnaire(person_id: str,course_id: str):
    return await personal_service.get_personal_questionnaire(person_id,course_id)