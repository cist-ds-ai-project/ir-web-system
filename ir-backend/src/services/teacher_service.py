from src.models.teacher import (
    TeacherCourseInfo,
    TeacherCourseResponse
)

from src.datasource import irweb_irweb


async def get_teacher_courses(person_id: str) -> TeacherCourseResponse:

    db = irweb_irweb()

    sql = """
        SELECT
            c.course_id,
            c.lecture_name,
            c.available_year,
            s.semester,
            cc.category_name,
            c.number_credits_course,
            COUNT(DISTINCT se.student_id) AS student_count
        FROM data.course_instructors ci
        JOIN data.courses c
            ON ci.course_id = c.course_id
        LEFT JOIN data.semesters s
            ON c.available_semester = s.id
        LEFT JOIN data.course_categories cc
            ON c.course_category = cc.id
        LEFT JOIN data.student_engagement se
            ON c.course_id = se.course_id
        WHERE ci.lecture_teacher = %s
        GROUP BY
            c.course_id,
            c.lecture_name,
            c.available_year,
            s.semester,
            cc.category_name,
            c.number_credits_course
        ORDER BY
            c.available_year DESC,
            c.lecture_name
    """

    df = await db.query_sql(sql, person_id)

    if df is None or df.empty:
        return TeacherCourseResponse(
            person_id=person_id,
            courses=[]
        )

    courses = [
        TeacherCourseInfo(
            course_id=row["course_id"],
            lecture_name=row["lecture_name"],
            year=(
                int(row["available_year"])
                if row["available_year"] is not None
                else None
            ),
            semester=row["semester"],
            category=row["category_name"],
            credits_course=(
                int(row["number_credits_course"])
                if row["number_credits_course"] is not None
                else None
            ),
            student_count=int(row["student_count"])
        )
        for _, row in df.iterrows()
    ]

    return TeacherCourseResponse(
        person_id=person_id,
        courses=courses
    )