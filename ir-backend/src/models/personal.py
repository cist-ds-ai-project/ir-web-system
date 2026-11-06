from pydantic import BaseModel


class PersonalGradeInfo(BaseModel):
    """個人成績1科目分のモデル"""
    course_id: str
    lecture_name: str
    lecture_teacher: str | None = None
    year: int | None = None
    category: str | None = None
    credits_course: int | None = None

    grading: str | None = None
    gp: float | None = None

    attendance: int | None = None
    lateness: int | None = None
    absence: int | None = None
    missing: int | None = None
    semester: str | None = None


class PersonalGradeResponse(BaseModel):
    """個人成績一覧のレスポンスモデル"""
    person_id: str
    grades: list[PersonalGradeInfo]

class PersonalSelectionOptionsResponse(BaseModel):
    years: list[int]
    semesters: list[str]
    categories: list[str]

class PersonalGpaInfo(BaseModel):
    year: int
    semester: str
    gpa: float

class PersonalGpaResponse(BaseModel):
    person_id: str
    gpas: list[PersonalGpaInfo]

class PersonalQuestionnaire(BaseModel):
    question_id: int
    question_title: str
    question_text: str
    question_type: str
    answer: str | None

class PersonalQuestionnaireResponse(BaseModel):
    course_id: str
    lecture_name: str | None
    questionnaire: list[PersonalQuestionnaire]