from src.models.personal import (
    PersonalGradeInfo,
    PersonalGradeResponse,
    PersonalSelectionOptionsResponse,
    PersonalGpaInfo,
    PersonalGpaResponse, PersonalQuestionnaireResponse, PersonalQuestionnaire
)
from src.datasource import irweb_irweb

async def get_selection_options():
    db = irweb_irweb()

    sql_year = """
        SELECT DISTINCT available_year
        FROM data.courses
        ORDER BY available_year
    """

    sql_semester = """
        SELECT DISTINCT semester
        FROM data.semesters
        ORDER BY semester
    """

    sql_category = """
        SELECT category_name
        FROM data.course_categories
        ORDER BY id
    """

    sql_grade = """
                SELECT grading
                FROM data.grading_standards
                ORDER BY id 
                """

    df_year = await db.query_sql(sql_year)
    df_semester = await db.query_sql(sql_semester)
    df_category = await db.query_sql(sql_category)
    df_grade = await db.query_sql(sql_grade)

    return {
        "years": df_year["available_year"].tolist(),
        "semesters": df_semester["semester"].tolist(),
        "categories": df_category["category_name"].tolist(),
        "grade": df_grade["grading"].tolist()
    }


async def get_personal_options() -> PersonalSelectionOptionsResponse:
    """個人成績画面の学期と必選区分の選択肢を取得する"""

    db = irweb_irweb()

    semester_sql = """
        SELECT DISTINCT TRIM(semester) AS semester
        FROM data.semesters
        WHERE NULLIF(TRIM(semester), '') IS NOT NULL
        ORDER BY semester
    """

    category_sql = """
        SELECT DISTINCT TRIM(category_name) AS category
        FROM data.course_categories
        WHERE NULLIF(TRIM(category_name), '') IS NOT NULL
        ORDER BY category
    """

    semester_df = await db.query_sql(semester_sql)
    category_df = await db.query_sql(category_sql)

    semesters = (
        semester_df["semester"].astype(str).tolist()
        if semester_df is not None and not semester_df.empty
        else []
    )
    categories = (
        category_df["category"].astype(str).tolist()
        if category_df is not None and not category_df.empty
        else []
    )

    return PersonalSelectionOptionsResponse(
        semesters=semesters,
        categories=categories,
    )


async def get_personal_grades(person_id: str)-> PersonalGradeResponse:

    """person_id に紐づく個人成績を取得する"""

    db = irweb_irweb()

    sql = """
          SELECT c.course_id, 
                 c.lecture_name, 
                 ci.lecture_teacher, 
                 c.available_year, 
                 cc.category_name, 
                 c.number_credits_course, 
                 gs.grading, 
                 gs.gp, 
                 se.attendance,
                 se.lateness, 
                 se.absence, 
                 se.missing,
                 s.semester
          FROM data.student_engagement se
                   JOIN data.courses c
                        ON se.course_id = c.course_id
                   LEFT JOIN ( 
                        SELECT 
                            ci.course_id,
                            STRING_AGG(p.name_last || '' ||p.name_first, 
                                ', ' 
                                ORDER BY p.name_last,p.name_first) AS lecture_teacher
                        FROM data.course_instructors ci
                        JOIN data.persons p
                            ON ci.lecture_teacher = p.person_id
                        GROUP BY ci.course_id
                   ) ci
                        ON se.course_id = ci.course_id
                   LEFT JOIN data.course_categories cc
                             ON c.course_category = cc.id
                   LEFT JOIN data.grading_standards gs
                             ON se.grading::integer = gs.id
                   LEFT JOIN data.semesters s
                             ON c.available_semester = s.id
          WHERE se.student_id = %s
          ORDER BY c.available_year, c.lecture_name 
          """

    df = await db.query_sql(sql, person_id)

    if df is None or df.empty:
        return PersonalGradeResponse(
            person_id=person_id,
            grades=[]
        )

    grades = [
        PersonalGradeInfo(
            course_id=row["course_id"],
            lecture_name=row["lecture_name"],
            lecture_teacher=row["lecture_teacher"],
            year=int(row["available_year"]),
            category=row["category_name"],
            credits_course=int(row["number_credits_course"]),
            grading=row["grading"],
            gp=float(row["gp"]),
            attendance=int(row["attendance"]),
            lateness=int(row["lateness"]),
            absence=int(row["absence"]),
            missing=int(row["missing"]),
            semester=row["semester"],
        )
        for _, row in df.iterrows()
    ]

    return PersonalGradeResponse(
        person_id=person_id,
        grades=grades
    )

async def get_gpa_by_year_semester(person_id: str) -> PersonalGpaResponse:
    response = await get_personal_grades(person_id)

    grades = response.grades

    grouped = {}

    for grade in grades:
        if grade.gp is None or grade.credits_course is None:
            continue

        key = (grade.year, grade.semester)

        if key not in grouped:
            grouped[key] = {
                "total_gp_credits": 0,
                "total_credits": 0
            }

        grouped[key]["total_gp_credits"] += (grade.gp * grade.credits_course)

        grouped[key]["total_credits"] += grade.credits_course

    gpas = []

    for (year, semester), data in grouped.items():

        if data["total_credits"] == 0:
            continue

        gpa = (data["total_gp_credits"] / data["total_credits"])

        gpas.append(PersonalGpaInfo(
                year=year,
                semester=semester,
                gpa=round(gpa, 2)
            )
        )

    gpas.sort(key=lambda x: (x.year, x.semester))

    return PersonalGpaResponse(person_id=person_id,gpas=gpas)

async def get_personal_questionnaire(person_id: str, course_id: str) -> PersonalQuestionnaireResponse:
    db = irweb_irweb()

    sql = """
        SELECT
            q.question_id,
            q.question_title,
            q.question_text,
            q.question_type,
            a.answer,
            c.course_id,
            c.lecture_name
        FROM data.answers a 
        JOIN data.questions q
            ON a.question_id = q.question_id
        JOIN data.courses c
            ON q.course_id = c.course_id
        WHERE a.respondent_id = %s
          AND q.course_id = %s
        ORDER BY q.question_id
    """

    df = await db.query_sql(sql, person_id, course_id)

    if df is None or df.empty:
        return PersonalQuestionnaireResponse(
            course_id=course_id,
            lecture_name=None,
            questionnaire=[]
        )

    questionnaire = [
        PersonalQuestionnaire(
            question_id=int(row["question_id"]),
            question_title=row["question_title"],
            question_text=row["question_text"],
            question_type=row["question_type"],
            answer=row["answer"]
        )
        for _, row in df.iterrows()
    ]

    return PersonalQuestionnaireResponse(
        course_id=course_id,
        lecture_name=df.iloc[0]["lecture_name"],
        questionnaire=questionnaire
    )