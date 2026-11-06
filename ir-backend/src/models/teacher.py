from pydantic import BaseModel


class TeacherCourseInfo(BaseModel):
    course_id: str
    lecture_name: str
    year: int | None = None
    semester: str | None = None
    category: str | None = None
    credits_course: int | None = None
    student_count: int = 0


class TeacherCourseResponse(BaseModel):
    person_id: str
    courses: list[TeacherCourseInfo]

