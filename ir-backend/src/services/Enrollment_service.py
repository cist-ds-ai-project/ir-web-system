import pandas as pd
from src.datasource import irweb_irweb
from src.services import grade_service
import math


def _clean_value(value):
    if pd.isna(value):
        return None
    if isinstance(value, str):
        value = value.strip()
        return value if value else None
    if hasattr(value, "item"):
        return value.item()
    return value


def _semester_label(available_semester) -> str:
    if available_semester == 1:
        return "前期"
    if available_semester == 2:
        return "後期"
    return str(available_semester)


async def get_all_lectures() -> list[dict]:
    """すべての講義情報を一括で取得する"""
    db = irweb_irweb()

    sql = """
        WITH latest_courses AS (
            SELECT
                c.course_id,
                c.available_semester,
                c.lecture_name,
                c.target_grade,
                c.target_department,
                cc.category_name AS category,
                c.number_credits_course AS credits_course,
                ROW_NUMBER() OVER (
                    PARTITION BY TRIM(c.lecture_name), c.available_semester
                    ORDER BY c.available_year DESC, c.course_id DESC
                ) AS row_number
            FROM data.courses c
            LEFT JOIN data.course_categories cc
                ON c.course_category = cc.id
            WHERE c.lecture_name IS NOT NULL
        )
        SELECT
            lc.course_id,
            lc.available_semester,
            lc.lecture_name,
            lc.target_grade,
            lc.target_department,
            lc.category,
            lc.credits_course,
            COALESCE(
                STRING_AGG(DISTINCT p.name_last || p.name_first, '、'),
                '不明'
            ) AS lecture_teacher
        FROM latest_courses lc
        LEFT JOIN data.course_instructors ct
            ON lc.course_id = ct.course_id
        LEFT JOIN data.persons p
            ON ct.lecture_teacher = p.person_id
        WHERE lc.row_number = 1
        GROUP BY
            lc.course_id,
            lc.available_semester,
            lc.lecture_name,
            lc.target_grade,
            lc.target_department,
            lc.category,
            lc.credits_course
        ORDER BY lc.lecture_name, lc.available_semester
    """

    df = await db.query_sql(sql)

    if df is None or df.empty:
        return []

    records = []
    for _, row in df.iterrows():
        records.append({
            "course_id": _clean_value(row["course_id"]),
            "available_semester": _clean_value(row["available_semester"]),
            "lecture_name": _clean_value(row["lecture_name"]),
            "lecture_teacher": _clean_value(row["lecture_teacher"]) or "不明",
            "target_grade": _clean_value(row["target_grade"]),
            "target_department": _clean_value(row["target_department"]),
            "category": _clean_value(row["category"]),
            "credits_course": _clean_value(row["credits_course"]),
        })

    return records


async def get_categories() -> list[dict]:
    return await grade_service.get_categories()


async def get_selection_options() -> dict:
    db = irweb_irweb()

    sql_grades = """
        SELECT DISTINCT TRIM(target_grade) AS target_grade
        FROM data.courses
        WHERE NULLIF(TRIM(target_grade), '') IS NOT NULL
        ORDER BY TRIM(target_grade)
    """
    df_grades = await db.query_sql(sql_grades)

    sql_departments = """
        SELECT DISTINCT TRIM(target_department) AS target_department
        FROM data.courses
        WHERE NULLIF(TRIM(target_department), '') IS NOT NULL
        ORDER BY TRIM(target_department)
    """
    df_departments = await db.query_sql(sql_departments)

    sql_categories = """
        SELECT DISTINCT TRIM(category_name) AS category
        FROM data.course_categories
        WHERE NULLIF(TRIM(category_name), '') IS NOT NULL
        ORDER BY TRIM(category_name)
    """
    df_categories = await db.query_sql(sql_categories)

    response = {
        "grades": ["全体"],
        "departments": ["全体"],
        "categories": ["全体"],
    }

    if df_grades is not None and not df_grades.empty:
        response["grades"] += [
            str(grade)
            for grade in df_grades["target_grade"].dropna().tolist()
        ]

    if df_departments is not None and not df_departments.empty:
        response["departments"] += [
            str(department)
            for department in df_departments["target_department"].dropna().tolist()
        ]

    if df_categories is not None and not df_categories.empty:
        response["categories"] += [
            str(category)
            for category in df_categories["category"].dropna().tolist()
        ]

    return response


async def get_enrollment_trend(lecture_name: str) -> dict:
    """
    講義名から履修者数の年度推移を取得します。
    course_id 指定に移行した後も、既存画面遷移の互換用として残します。
    """
    db = irweb_irweb()
    trimmed_lecture_name = lecture_name.strip()

    sql_students = """
        SELECT
            c.available_year,
            COUNT(DISTINCT se.student_id) AS students
        FROM data.courses c
        JOIN data.student_engagement se
            ON c.course_id = se.course_id
        WHERE TRIM(c.lecture_name) = %s
        GROUP BY c.available_year
        ORDER BY c.available_year
    """
    df_students = await db.query_sql(sql_students, trimmed_lecture_name)

    if df_students is None or df_students.empty:
        return {
            "lecture_name": trimmed_lecture_name,
            "distributions": []
        }

    sql_available_student = """
        SELECT
            c.available_year,
            COUNT(DISTINCT s.student_id) AS available_student
        FROM data.courses c
        JOIN data.student_status ss
            ON ss.year = c.available_year
            AND (
                NULLIF(TRIM(c.target_grade), '') IS NULL
                OR TRIM(ss.academic_year) = TRIM(c.target_grade)
            )
        JOIN data.students s
            ON s.student_id = ss.student_id
        WHERE TRIM(c.lecture_name) = %s
            AND (
                NULLIF(TRIM(c.target_faculty), '') IS NULL
                OR TRIM(s.faculty_name) = TRIM(c.target_faculty)
            )
            AND (
                NULLIF(TRIM(c.target_department), '') IS NULL
                OR TRIM(s.department_name) = TRIM(c.target_department)
            )
            AND (
                NULLIF(TRIM(c.target_course), '') IS NULL
                OR TRIM(s.course_name) = TRIM(c.target_course)
            )
        GROUP BY c.available_year
        ORDER BY c.available_year
    """
    df_available_student = await db.query_sql(sql_available_student, trimmed_lecture_name)

    available_student_map = {}
    if df_available_student is not None and not df_available_student.empty:
        available_student_map = {
            int(row["available_year"]): int(row["available_student"] or 0)
            for _, row in df_available_student.iterrows()
        }

    sql_teachers = """
        SELECT DISTINCT
            c.available_year,
            p.name_last || p.name_first AS teacher_name
        FROM data.courses c
        JOIN data.course_instructors ct
            ON c.course_id = ct.course_id
        JOIN data.persons p
            ON ct.lecture_teacher = p.person_id
        WHERE TRIM(c.lecture_name) = %s
        ORDER BY c.available_year
    """
    df_teachers = await db.query_sql(sql_teachers, trimmed_lecture_name)

    teacher_map = {}
    if df_teachers is not None and not df_teachers.empty:
        teacher_map = (
            df_teachers.groupby("available_year")["teacher_name"]
            .apply(lambda x: "、".join(x.dropna().unique()))
            .to_dict()
        )

    students_list = df_students["students"].tolist()
    total_years = len(students_list)

    if total_years > 0:
        average_students = sum(students_list) / total_years
        variance_students = (
            sum((x - average_students) ** 2 for x in students_list)
            / total_years
        )
        standard_deviation_students = math.sqrt(max(variance_students, 0.0))
    else:
        average_students = 0.0
        standard_deviation_students = 0.0

    distributions = []
    for _, row in df_students.iterrows():
        year = int(row["available_year"])
        students_count = int(row["students"])
        available_student = available_student_map.get(year, 0)
        enrollment_rate = (
            round((students_count / available_student) * 100, 2)
            if available_student > 0
            else 0.0
        )

        distributions.append({
            "year": year,
            "counts": {"履修者数": students_count},
            "total_students": students_count,
            "available_student": available_student,
            "enrollment_rate": enrollment_rate,
            "lecture_teacher": teacher_map.get(year, "不明"),
            "percentages": {"履修者数": 100.0},
            "average": round(average_students, 2),
            "standard_deviation": round(standard_deviation_students, 2)
        })

    return {
        "lecture_name": trimmed_lecture_name,
        "distributions": sorted(distributions, key=lambda x: x["year"], reverse=True)
    }


async def get_enrollment_trend_by_course_id(course_id: int | str) -> dict:
    """
    course_id から講義名と開講期を取得し、
    同じ lecture_name + available_semester の年度推移を取得します。
    """
    db = irweb_irweb()

    # 選択された course_id の講義情報を取得
    sql_base_course = """
        SELECT
            lecture_name,
            available_semester
        FROM data.courses
        WHERE course_id = %s
    """
    df_base_course = await db.query_sql(sql_base_course, course_id)

    if df_base_course is None or df_base_course.empty:
        return {
            "lecture_name": "",
            "distributions": []
        }

    base = df_base_course.iloc[0]

    trimmed_lecture_name = str(base["lecture_name"]).strip()

    if pd.isna(base["available_semester"]):
        return {
            "lecture_name": trimmed_lecture_name,
            "distributions": []
        }

    available_semester = int(base["available_semester"])

    semester_label = _semester_label(available_semester)
    display_lecture_name = f"{trimmed_lecture_name}（{semester_label}）"

    # 履修者数を年度ごとに取得
    sql_students = """
        SELECT
            c.available_year,
            COUNT(DISTINCT se.student_id) AS students
        FROM data.courses c
        JOIN data.student_engagement se
            ON c.course_id = se.course_id
        WHERE TRIM(c.lecture_name) = %s
            AND c.available_semester = %s
        GROUP BY c.available_year
        ORDER BY c.available_year
    """
    df_students = await db.query_sql(
        sql_students,
        trimmed_lecture_name,
        available_semester
    )

    if df_students is None or df_students.empty:
        return {
            "lecture_name": display_lecture_name,
            "available_semester": available_semester,
            "distributions": []
        }

    # 分母になる「履修可能な学生数」を年度ごとに取得
    sql_available_student = """
        SELECT
            c.available_year,
            COUNT(DISTINCT s.student_id) AS available_student
        FROM data.courses c
        JOIN data.student_status ss
            ON ss.year = c.available_year
            AND (
                NULLIF(TRIM(c.target_grade), '') IS NULL
                OR TRIM(ss.academic_year) = TRIM(c.target_grade)
            )
        JOIN data.students s
            ON s.student_id = ss.student_id
        WHERE TRIM(c.lecture_name) = %s
            AND c.available_semester = %s
            AND (
                NULLIF(TRIM(c.target_faculty), '') IS NULL
                OR TRIM(s.faculty_name) = TRIM(c.target_faculty)
            )
            AND (
                NULLIF(TRIM(c.target_department), '') IS NULL
                OR TRIM(s.department_name) = TRIM(c.target_department)
            )
            AND (
                NULLIF(TRIM(c.target_course), '') IS NULL
                OR TRIM(s.course_name) = TRIM(c.target_course)
            )
        GROUP BY c.available_year
        ORDER BY c.available_year
    """
    df_available_student = await db.query_sql(
        sql_available_student,
        trimmed_lecture_name,
        available_semester
    )

    available_student_map = {}
    if df_available_student is not None and not df_available_student.empty:
        available_student_map = {
            int(row["available_year"]): int(row["available_student"] or 0)
            for _, row in df_available_student.iterrows()
        }

    # 担当教員を年度ごとに取得
    sql_teachers = """
        SELECT DISTINCT
            c.available_year,
            p.name_last || p.name_first AS teacher_name
        FROM data.courses c
        JOIN data.course_instructors ct
            ON c.course_id = ct.course_id
        JOIN data.persons p
            ON ct.lecture_teacher = p.person_id
        WHERE TRIM(c.lecture_name) = %s
            AND c.available_semester = %s
        ORDER BY c.available_year
    """
    df_teachers = await db.query_sql(
        sql_teachers,
        trimmed_lecture_name,
        available_semester
    )

    teacher_map = {}
    if df_teachers is not None and not df_teachers.empty:
        teacher_map = (
            df_teachers.groupby("available_year")["teacher_name"]
            .apply(lambda x: "、".join(x.dropna().unique()))
            .to_dict()
        )

    students_list = df_students["students"].tolist()
    total_years = len(students_list)

    if total_years > 0:
        average_students = sum(students_list) / total_years
        variance_students = (
            sum((x - average_students) ** 2 for x in students_list)
            / total_years
        )
        standard_deviation_students = math.sqrt(max(variance_students, 0.0))
    else:
        average_students = 0.0
        standard_deviation_students = 0.0

    distributions = []
    for _, row in df_students.iterrows():
        year = int(row["available_year"])
        students_count = int(row["students"])
        available_student = available_student_map.get(year, 0)
        enrollment_rate = (
            round((students_count / available_student) * 100, 2)
            if available_student > 0
            else 0.0
        )

        distributions.append({
            "year": year,
            "counts": {"履修者数": students_count},
            "total_students": students_count,
            "available_student": available_student,
            "enrollment_rate": enrollment_rate,
            "lecture_teacher": teacher_map.get(year, "不明"),
            "percentages": {"履修者数": 100.0},
            "average": round(average_students, 2),
            "standard_deviation": round(standard_deviation_students, 2)
        })

    return {
        "lecture_name": display_lecture_name,
        "available_semester": available_semester,
        "distributions": sorted(distributions, key=lambda x: x["year"], reverse=True)
    }
