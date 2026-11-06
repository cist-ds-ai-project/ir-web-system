import pandas as pd
from starlette.exceptions import HTTPException
from src.datasource import irweb_irweb

#年度学科等の選択肢
async def get_selection_options() -> dict:
    db = irweb_irweb()

    sql_years = "SELECT DISTINCT year FROM data.student_status"
    sql_faculties = "SELECT DISTINCT faculty_name FROM data.students"
    sql_departments = "SELECT DISTINCT department_name FROM data.students"
    sql_grades = "SELECT DISTINCT academic_year FROM data.student_status"

    df_years = await db.query_sql(sql_years)
    df_faculties = await db.query_sql(sql_faculties)
    df_departments = await db.query_sql(sql_departments)
    df_grades = await db.query_sql(sql_grades)

    response = {
        "years": [],
        "faculties": ["全体"],
        "departments": ["全体"],
        "grades": ["全体"],
    }

    if df_years is not None and not df_years.empty:
        response["years"] += sorted(df_years["year"].dropna().astype(int).unique().tolist())

    if df_faculties is not None and not df_faculties.empty:
        response["faculties"] += sorted(
            df_faculties["faculty_name"].dropna().astype(str).unique().tolist()
        )

    if df_departments is not None and not df_departments.empty:
        response["departments"] += sorted(
            df_departments["department_name"].dropna().astype(str).unique().tolist()
        )

    if df_grades is not None and not df_grades.empty:
        response["grades"] += sorted(
            df_grades["academic_year"].dropna().astype(str).unique().tolist()
        )

    return response

#frontendで選ばれた条件を
def apply_filters(filter, conditions, params, include_department=True):

    # 年度(複数選択)
    if filter.years:
        placeholders = ",".join(["%s"] * len(filter.years))
        conditions.append(f"ss.year IN ({placeholders})")
        params.extend(filter.years)

    # 学部
    if filter.faculty and filter.faculty != "全体":
        conditions.append("s.faculty_name = %s")
        params.append(filter.faculty)

    # 学科(複数選択)
    if include_department and filter.departments:
        placeholders = ",".join(["%s"] * len(filter.departments))
        conditions.append(f"s.department_name IN ({placeholders})")
        params.extend(filter.departments)

    # 学年
    if filter.grades:
        grades = [g for g in filter.grades
                  if g != "全体"]
        if grades:
            placeholders = ",".join(["%s"] * len(grades))
            conditions.append(f"ss.academic_year IN ({placeholders})")
            params.extend(grades)



#ヒストグラム用GPAデータ
async def get_raw_gpa(filter) -> dict:
    db = irweb_irweb()

    #sql = """
        #SELECT ss.gpa
        #FROM data.student_status ss
        #JOIN data.students s ON ss.student_id = s.student_id
        #WHERE ss.gpa IS NOT NULL
    #"""

    sql = """
          SELECT ss.student_id, 
                 s.faculty_name,
                 s.department_name, 
                 ss.year, 
                 ss.semester, 
                 ss.gpa, 
                 ss.academic_year
          FROM data.student_status ss
                   JOIN data.students s
                        ON ss.student_id = s.student_id
          WHERE ss.gpa IS NOT NULL 
          """

    conditions = []
    params = []
    apply_filters(filter, conditions, params,include_department=False)

    if conditions:
        sql += " AND " + " AND ".join(conditions)

    df = await db.query_sql(sql, *params)
    return {
        "gpas": df["gpa"].tolist() if df is not None else []
    }




#学科別GPAデータ
async def get_gpa_by_department(filter) -> dict:
    db = irweb_irweb()

    sql = """
        SELECT s.department_name, ss.gpa
        FROM data.student_status ss
        JOIN data.students s ON ss.student_id = s.student_id
        WHERE ss.gpa IS NOT NULL
    """

    conditions = []
    params = []

    apply_filters(filter, conditions, params, include_department=True)

    if conditions:
        sql += " AND " + " AND ".join(conditions)

    df = await db.query_sql(sql, *params)

    if df is None or df.empty:
        return {"departments": []}

    grouped = df.groupby("department_name")["gpa"].apply(list).to_dict()

    return {
        "departments": [
            {
                "department": dept,
                "gpas": [float(x) for x in values]
            }
            for dept, values in grouped.items()
        ]
    }

#統計データ
async def get_stats(filter) -> dict:
    db = irweb_irweb()

    sql = """
        SELECT s.department_name, ss.gpa
        FROM data.student_status ss
        JOIN data.students s ON ss.student_id = s.student_id
        WHERE ss.gpa IS NOT NULL
    """

    conditions = []
    params = []

    apply_filters(filter, conditions, params,include_department=False)

    if conditions:
        sql += " AND " + " AND ".join(conditions)

    df = await db.query_sql(sql, *params)

    if df is None or df.empty:
        return {
            "overall": {
                "count": 0,
                "avg": 0,
                "min": 0,
                "max": 0,
                "std": 0
            },

            "by_department": []
        }

    # 全体
    gpas = df["gpa"].astype(float)

    overall = {
        "count": int(len(gpas)),
        "avg": round(float(gpas.mean()), 2),
        "min": round(float(gpas.min()), 2),
        "max": round(float(gpas.max()), 2),
        "std": round(float(gpas.std(ddof=0)), 2)
    }

    # 学科別
    by_department = []
    for dept, group in df.groupby("department_name"):
        g = group["gpa"].astype(float)

        by_department.append({
            "department": dept,
            "stats": {
                "count": int(len(g)),
                "avg": round(float(g.mean()), 2),
                "min": round(float(g.min()), 2),
                "max": round(float(g.max()), 2),
                "std": round(float(g.std(ddof=0)), 2)
            }
        })

    return {
        "overall": overall,
        "by_department": by_department
    }

