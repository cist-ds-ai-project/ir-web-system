from pydantic import BaseModel
from typing import Dict


class SelectionOptionsResponse(BaseModel):
    grades: list[str]
    departments: list[str]
    categories: list[str]


class LectureInfo(BaseModel):
    course_id: int | str | None = None
    available_semester: int | None = None
    lecture_name: str
    lecture_teacher: str | None = None
    target_grade: str | None = None
    target_department: str | None = None
    category: str | None = None
    credits_course: int | None = None


class LectureListResponse(BaseModel):
    lectures: list[LectureInfo]


class YearlyEnrollment(BaseModel):
    year: int
    lecture_teacher: str
    counts: Dict[str, int]
    total_students: int
    available_student: int | None = None
    enrollment_rate: float | None = None
    percentages: Dict[str, float]
    average: float
    standard_deviation: float


class EnrollmentTrendResponse(BaseModel):
    lecture_name: str
    distributions: list[YearlyEnrollment]


class CategoryItem(BaseModel):
    value: int
    label: str


class CategoryListResponse(BaseModel):
    categories: list[CategoryItem]
