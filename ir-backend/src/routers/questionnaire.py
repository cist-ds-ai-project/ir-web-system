from fastapi import APIRouter
from src.services.questionnaire_service import get_all_questions, \
    get_all_answers

router = APIRouter()

@router.get("/questionnaire/{lecture_name}")
async def get_question_info(lecture_name: str):
    question_info = await get_all_questions(
        lecture_name
    )

    questions = await get_all_answers(
        question_info["questions"],
        question_info.get("differing_columns", []),
        lecture_name
    )

    print(question_info)
    return {
        "lecture_name": lecture_name,
        "questions": questions
    }