from src.datasource import irweb_irweb
from typing import List, Optional
import pandas as pd


async def get_scholarship_distribution(
        type: str = None,
        academic_year: str = None,
        faculty: str = None,
        department: str = None
):
    db = irweb_irweb()

    conditions = ["scholarship IS NOT NULL"]
    params = []

    if type and type != "全体":
        conditions.append("scholarship = %s")
        params.append(type)

    if academic_year and academic_year != "全体":
        conditions.append("academic_year = %s")
        params.append(academic_year)

    if faculty and faculty != "全体":
        conditions.append("faculty_name = %s")
        params.append(faculty)

    if department and department != "全体":
        conditions.append("department_name = %s")
        params.append(department)

    where_clause = "WHERE " + " AND ".join(conditions)

    query = f"""
        SELECT
            scholarship,
            COUNT(*) AS count
        FROM data.students
        {where_clause}
        GROUP BY scholarship
        ORDER BY scholarship;
    """

    if params:
        df = await db.query_sql(query, *params)
    else:
        df = await db.query_sql(query)

    if df is None:
        df = pd.DataFrame(columns=['scholarship', 'count'])

    df = df.dropna(subset=["scholarship"])

    return {
        "labels": df["scholarship"].tolist(),
        "data": df["count"].tolist(),
        "total": int(df["count"].sum())
    }


async def get_scholarship_trend(
        type: str = None,
        faculty: str = None,
        department: str = None
) -> dict:
    """受給者推移（入学年度別）を取得する"""
    db = irweb_irweb()

    conditions = ["scholarship IS NOT NULL", "enrollment_date IS NOT NULL"]
    params = []

    if type and type != "全体":
        conditions.append("scholarship = %s")
        params.append(type)
    if faculty and faculty != "全体":
        conditions.append("faculty_name = %s")
        params.append(faculty)
    if department and department != "全体":
        conditions.append("department_name = %s")
        params.append(department)

    where_clause = "WHERE " + " AND ".join(conditions)

    query = f"""
        SELECT
            TO_CHAR(enrollment_date, 'YYYY') AS enrollment_year,
            scholarship,
            COUNT(*) AS count
        FROM data.students
        {where_clause}
        GROUP BY TO_CHAR(enrollment_date, 'YYYY'), scholarship
        ORDER BY enrollment_year ASC, scholarship ASC;
    """

    if params:
        df = await db.query_sql(query, *params)
    else:
        df = await db.query_sql(query)

    if df is None or df.empty:
        return {"data": [], "lines": []}

    df_pivot = df.pivot(index='enrollment_year', columns='scholarship', values='count').fillna(0)
    df_pivot.reset_index(inplace=True)

    lines = [col for col in df_pivot.columns if col != 'enrollment_year']

    return {
        "data": df_pivot.to_dict(orient='records'),
        "lines": lines
    }


async def get_scholarship_types() -> List[str]:
    db = irweb_irweb()
    query = "SELECT DISTINCT scholarship FROM data.students WHERE scholarship IS NOT NULL ORDER BY scholarship"
    df = await db.query_sql(query)
    if df is None:
        return ["全体"]
    return ["全体"] + df["scholarship"].tolist()


async def get_academic_years() -> List[str]:
    db = irweb_irweb()
    query = "SELECT DISTINCT academic_year FROM data.students WHERE academic_year IS NOT NULL ORDER BY academic_year DESC"
    df = await db.query_sql(query)
    if df is None:
        return ["全体"]
    return ["全体"] + df["academic_year"].tolist()


async def get_faculties() -> List[str]:
    db = irweb_irweb()
    query = "SELECT DISTINCT faculty_name FROM data.students WHERE faculty_name IS NOT NULL ORDER BY faculty_name"
    df = await db.query_sql(query)
    if df is None:
        return ["全体"]
    return ["全体"] + df["faculty_name"].tolist()


async def get_departments(faculty: str = None) -> List[str]:
    db = irweb_irweb()
    df = None
    if faculty and faculty != "全体":
        query = "SELECT DISTINCT department_name FROM data.students WHERE faculty_name = %s AND department_name IS NOT NULL ORDER BY department_name"
        params = (faculty,)
        df = await db.query_sql(query, *params)
    else:
        query = "SELECT DISTINCT department_name FROM data.students WHERE department_name IS NOT NULL ORDER BY department_name"
        df = await db.query_sql(query)

    if df is None:
        return ["全体"]
    return ["全体"] + df["department_name"].tolist()


async def get_enrollment_date() -> List[str]:
    db = irweb_irweb()
    query = """
            SELECT DISTINCT TO_CHAR(enrollment_date, 'YYYY') AS enrollment_year
            FROM data.students
            WHERE enrollment_date IS NOT NULL
            ORDER BY enrollment_year ASC
            """
    df = await db.query_sql(query)
    if df is None:
        return ["全体"]
    return ["全体"] + df["enrollment_year"].tolist()