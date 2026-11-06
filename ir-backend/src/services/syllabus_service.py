from src.datasource import irweb_irweb


async def get_syllabus_detail(course_id: str) -> dict | None:
    """シラバス詳細を1科目分まとめて取得する"""
    db = irweb_irweb()

    # --- 科目基本情報 ---
    # 授業形態: courses側に対応する列がまだ無いため、現状は取得していません。
    #           列が追加された際は SELECT 句と models/syllabus.py に追加してください。
    sql_course = """
        SELECT c.course_id,
               c.lecture_name,
               c.number_credits_course,
               c.target_grade
        FROM data.courses c
        WHERE c.course_id = %s
        """
    df_course = await db.query_sql(sql_course, course_id)

    if df_course is None or df_course.empty:
        return None

    course_row = df_course.iloc[0].to_dict()

    # --- 担当教員 ---
    # course_instructors.credit_accreditation_officer が True の教員を
    # 責任教員として is_responsible=True にする。表示上は1つのリストにまとめる。
    sql_teachers = """
        SELECT p.person_id,
               p.name_last || p.name_first AS teacher_name,
               t.department_name,
               ci.credit_accreditation_officer AS is_responsible
        FROM data.course_instructors ci
        JOIN data.persons p ON ci.lecture_teacher = p.person_id
        LEFT JOIN data.teachers t ON t.person_id = p.person_id
        WHERE ci.course_id = %s
        ORDER BY ci.credit_accreditation_officer DESC
        """
    df_teachers = await db.query_sql(sql_teachers, course_id)
    teachers = (
        df_teachers.to_dict(orient="records") if df_teachers is not None and not df_teachers.empty else []
    )

    # --- シラバス本文 ---
    sql_syllabus = """
        SELECT lecture_about,
               lecture_goal,
               study_outside_of_class,
               textbook,
               grading_standard_explain
        FROM data.syllabus
        WHERE course_id = %s
        """
    df_syllabus = await db.query_sql(sql_syllabus, course_id)
    syllabus_row = df_syllabus.iloc[0].to_dict() if df_syllabus is not None and not df_syllabus.empty else {}

    # --- 授業計画 ---
    sql_lecture_plans = """
        SELECT lecture_plan_id,
               lecture_plan
        FROM data.lecture_plans
        WHERE course_id = %s
        ORDER BY lecture_plan_id
        """
    df_lecture_plans = await db.query_sql(sql_lecture_plans, course_id)
    lecture_plans = (
        df_lecture_plans.to_dict(orient="records")
        if df_lecture_plans is not None and not df_lecture_plans.empty
        else []
    )

    # --- 評価項目(成績評価を割合の表で表示する場合) ---
    sql_evaluation = """
        SELECT evaluation_item_id,
               evaluation_item_title,
               evaluation_item_per
        FROM data.evaluation_items
        WHERE course_id = %s
        ORDER BY evaluation_item_id
        """
    df_evaluation = await db.query_sql(sql_evaluation, course_id)
    evaluation_items = (
        df_evaluation.to_dict(orient="records")
        if df_evaluation is not None and not df_evaluation.empty
        else []
    )

    return {
        **course_row,
        "teachers": teachers,
        **syllabus_row,
        "lecture_plans": lecture_plans,
        "evaluation_items": evaluation_items,
    }
