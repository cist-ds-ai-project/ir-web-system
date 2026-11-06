from src.datasource import irweb_irweb

async def get_selection_options() -> dict:
    db = irweb_irweb()

    sql_grades = "SELECT DISTINCT target_grade FROM data.courses"
    df_grades = await db.query_sql(sql_grades)

    sql_departments = "SELECT DISTINCT department_name FROM data.teachers"
    df_departments = await db.query_sql(sql_departments)

    sql_categories = "SELECT DISTINCT category_name FROM data.course_categories"
    df_categories = await db.query_sql(sql_categories)

    sql_semesters = "SELECT DISTINCT semester FROM data.semesters"
    df_semesters = await db.query_sql(sql_semesters)


    response = {
        "grades": ["全体"],
        "departments": ["全体"],
        "categories": ["全体"],
        "semesters": ["全体"]
    }

    if df_grades is not None and not df_grades.empty:
        raw_grades = df_grades["target_grade"].dropna().astype(str).unique()
        response["grades"] += sorted(list(raw_grades))

    if df_departments is not None and not df_departments.empty:
        raw_deps = sorted([str(d) for d in df_departments["department_name"].dropna().unique() if d])
        response["departments"] += raw_deps

    if df_categories is not None and not df_categories.empty:
        raw_cats = sorted([str(c) for c in df_categories["category_name"].dropna().unique() if c])
        response["categories"] += raw_cats

    if df_semesters is not None and not df_semesters.empty:
        raw_semes = [
            str(s) for s in df_semesters["semester"].dropna().unique()
            if s and s != "通年"
        ]
        response["semesters"] += raw_semes

    return response

async def get_all_lectures() -> list[dict]:
    """すべての講義情報を一括で取得する"""
    db = irweb_irweb()

    sql = """
          WITH ranked_courses AS (SELECT c.course_id, 
                                         c.lecture_name, 
                                         c.target_grade, 
                                         c.target_department, 
                                         cc.category_name        AS category, 
                                         c.number_credits_course AS credits_course, 
                                         s.semester              AS available_semester, 
                                         RANK()                     OVER(PARTITION BY c.lecture_name ORDER BY c.available_year DESC) as rank_number 
                                  FROM data.courses c 
                                           LEFT JOIN data.course_categories cc ON c.course_category = cc.id 
                                           LEFT JOIN data.semesters s ON c.available_semester = s.id 
                                  WHERE c.lecture_name IS NOT NULL)
          SELECT course_id, 
                 lecture_name, 
                 target_grade, 
                 target_department, 
                 category, 
                 credits_course, 
                 available_semester
          FROM ranked_courses
          WHERE rank_number = 1
          """

    df = await db.query_sql(sql)

    if df is None or df.empty:
        return []

    sql_teachers = """
          SELECT
              c.lecture_name,
              p.name_last || p.name_first AS teacher_name
          FROM data.courses c
          JOIN data.course_instructors ct ON c.course_id = ct.course_id
          JOIN data.persons p ON ct.lecture_teacher = p.person_id
          WHERE c.available_year = (
              SELECT MAX(c2.available_year)
              FROM data.courses c2
              WHERE c2.lecture_name = c.lecture_name
          )
          AND c.lecture_name IS NOT NULL
          """

    df_teachers = await db.query_sql(sql_teachers)

    # 科目ごとに教員名を「、」区切りで結合
    if df_teachers is not None and not df_teachers.empty:
        teacher_map = (
            df_teachers.groupby("lecture_name")["teacher_name"]
            .apply(lambda x: "、".join(x.dropna().unique().tolist()))
            .to_dict()
        )
    else:
        teacher_map = {}

    if "target_grade" in df.columns:
        df["target_grade"] = df["target_grade"].astype(str)

    if "target_department" in df.columns:
        df["target_department"] = df["target_department"].astype(str)

    records = df.to_dict(orient="records")
    for record in records:
        record["lecture_teacher"] = teacher_map.get(record["lecture_name"], "不明")

    return records