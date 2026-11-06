import json
from typing import Optional, List, Dict, Any

from src.datasource import irweb_irweb

async def questionnaire_courses_grid():
    """questionsからcourse_idをとり、授業データを全取得。アンケートにある授業のみをとるため、grade_serviceと差別化"""

    db = irweb_irweb()

    df = await db.query_sql(
        """SELECT DISTINCT ON (c.lecture_name) c.course_id, c.lecture_name, c.target_grade, c.target_faculty, 
        c.target_department, c.target_course, c.number_credits_course, cc.category_name, qs.questionnaire_id 
        FROM data.questions AS q LEFT JOIN data.courses AS c ON q.course_id = c.course_id 
        LEFT JOIN data.course_categories AS cc ON cc.id = c.course_category 
        LEFT JOIN data.questionnaire_structure AS qs ON qs.question_id = q.question_id WHERE q.course_id IS NOT NULL""")

    return {"courses": df.to_dict(orient="records")}

async def get_all_questions(
    lecture_name: Optional[str]
):
    db = irweb_irweb()

    plain_name = lecture_name.strip()

    # data.courses から該当講義の全レコードを取得し、差分があるカラムを抽出する
    df_courses = await db.query_sql(
        """SELECT * FROM data.courses WHERE TRIM(lecture_name) = %s""",
        plain_name
    )

    differing_columns = []
    if df_courses is not None and not df_courses.empty:
        # 比較対象から除外するキー
        exclude = {"course_id", "available_year", "lecture_name", "course_category"}
        for col in df_courses.columns:
            if col in exclude:
                continue
            # 値のバリエーション（重複を排除したユニーク値）を取得
            all_vals = df_courses[col].unique()
            if len(all_vals) > 1:
                differing_columns.append(col)

    df = await db.query_sql(
        """SELECT qs.questionnaire_id, ARRAY_AGG(qs.question_id ORDER BY qs.question_order) AS questions
        FROM data.questionnaire_structure qs JOIN data.questions q ON q.question_id = qs.question_id
        JOIN data.courses c ON c.course_id = q.course_id
        WHERE TRIM(c.lecture_name) = %s GROUP BY qs.questionnaire_id ORDER BY qs.questionnaire_id;
        """, plain_name)

    return {
        "questions": df.to_dict(orient="records") if df is not None else [],
        "differing_columns": differing_columns
    }


async def get_all_answers(
    data: Optional[List[Dict[str, Any]]],
    differing_columns: Optional[List[str]] = None,
    lecture_name: Optional[str] = None
):
    db = irweb_irweb()

    if not data:
        return []

    pairs = [
        {"questionnaire_id": row["questionnaire_id"],"question_id": q}
        for row in data for q in row["questions"]
    ]

    params = [json.dumps(pairs)]
    if differing_columns and lecture_name:
        params.append(lecture_name.strip())

    if differing_columns:
        # 動的に年度（＋差異のある項目）のSQL選択式を組み立てる
        year_select_expr = "EXTRACT(YEAR FROM q.start_date)::text"
        
        concat_parts = []
        for col in differing_columns:
            if col == "available_semester":
                part = "s.semester"
            else:
                part = f"COALESCE(c.{col}::text, '')"
            concat_parts.append(part)
        
        concat_expr = " || ' - ' || ".join(concat_parts)
        year_select_expr = f"({year_select_expr} || '(' || {concat_expr} || ')')"

        sql = f"""WITH input_pairs AS (SELECT * FROM jsonb_to_recordset(%s::jsonb) AS t(questionnaire_id text, question_id int)),
        qmeta AS (
            SELECT DISTINCT
                q.questionnaire_id, 
                qst.question_id,
                {year_select_expr} AS year 
            FROM data.questionnaires q
            JOIN data.questionnaire_structure qs ON q.questionnaire_id = qs.questionnaire_id
            JOIN data.questions qst ON qs.question_id = qst.question_id
            JOIN data.courses c ON qst.course_id = c.course_id
            LEFT JOIN data.semesters s ON c.available_semester = s.id
            WHERE TRIM(c.lecture_name) = %s
        ),
        base_choice AS (SELECT qm.year, a.question_id, o.option_label, o.option_order, COUNT(*) AS cnt FROM data.answers a 
            JOIN input_pairs ip ON ip.questionnaire_id = a.questionnaire_id AND ip.question_id = a.question_id JOIN data.questions q ON q.question_id = a.question_id 
            JOIN data.options o ON o.question_id = a.question_id AND o.option_label = a.answer JOIN qmeta qm ON qm.questionnaire_id = a.questionnaire_id AND qm.question_id = a.question_id 
            WHERE q.question_type = 'choice' GROUP BY qm.year, a.question_id, o.option_label, o.option_order),
        pivot_choice AS (SELECT year, question_id, jsonb_agg(jsonb_build_object('label', option_label,'count', cnt,'option_order', option_order) ORDER BY option_order)
            AS options FROM base_choice GROUP BY year, question_id),
        dataset_choice AS (SELECT question_id, jsonb_agg(jsonb_build_object('year', year,'options', options)ORDER BY year) AS dataset FROM pivot_choice GROUP BY question_id),
        base_text AS (SELECT a.year, a.question_id, COUNT(*) AS cnt, jsonb_agg(a.answer) FILTER (WHERE rn <= 50) AS answers 
            FROM (SELECT a.questionnaire_id, qm.year, a.question_id, BTRIM(a.answer) AS answer, ROW_NUMBER() OVER (PARTITION BY a.questionnaire_id, a.question_id, qm.year) AS rn
            FROM data.answers a JOIN input_pairs ip ON ip.questionnaire_id = a.questionnaire_id AND ip.question_id = a.question_id 
            JOIN qmeta qm ON qm.questionnaire_id = a.questionnaire_id AND qm.question_id = a.question_id JOIN data.questions q ON q.question_id = a.question_id
            WHERE q.question_type = 'text' AND a.answer IS NOT NULL AND BTRIM(a.answer) <> '') a GROUP BY a.year, a.question_id),
        dataset_text AS (SELECT question_id, jsonb_agg(jsonb_build_object('year', year, 'count', cnt, 'answers', answers) ORDER BY year) AS dataset 
            FROM base_text GROUP BY question_id),
        merged AS (SELECT q.question_id, q.question_text, q.question_title, q.question_type, COALESCE(dc.dataset, dt.dataset) AS dataset 
            FROM data.questions q JOIN input_pairs ip ON ip.question_id = q.question_id LEFT JOIN dataset_choice dc ON dc.question_id = q.question_id 
            LEFT JOIN dataset_text dt ON dt.question_id = q.question_id GROUP BY q.question_id, q.question_text, q.question_title, q.question_type, dc.dataset, dt.dataset)
        SELECT jsonb_build_object('question_id', question_id, 'question_Title', question_title, 'questionText', question_text,
                                  'question_type', question_type, 'dataset', dataset) AS result FROM merged ORDER BY question_id;"""
    else:
        # 開講学期が1種類のみの場合のSQL (year は String としてパースするため text へキャスト)
        sql = """WITH input_pairs AS (SELECT * FROM jsonb_to_recordset(%s::jsonb) AS t(questionnaire_id text, question_id int)),
        qmeta AS (SELECT questionnaire_id, EXTRACT(YEAR FROM start_date)::text AS year FROM data.questionnaires),
        base_choice AS (SELECT qm.year, a.question_id, o.option_label, o.option_order, COUNT(*) AS cnt FROM data.answers a 
            JOIN input_pairs ip ON ip.questionnaire_id = a.questionnaire_id AND ip.question_id = a.question_id JOIN data.questions q ON q.question_id = a.question_id 
            JOIN data.options o ON o.question_id = a.question_id AND o.option_label = a.answer JOIN qmeta qm ON qm.questionnaire_id = a.questionnaire_id 
            WHERE q.question_type = 'choice' GROUP BY qm.year, a.question_id, o.option_label, o.option_order),
        pivot_choice AS (SELECT year, question_id, jsonb_agg(jsonb_build_object('label', option_label,'count', cnt,'option_order', option_order) ORDER BY option_order)
            AS options FROM base_choice GROUP BY year, question_id),
        dataset_choice AS (SELECT question_id, jsonb_agg(jsonb_build_object('year', year,'options', options)ORDER BY year) AS dataset FROM pivot_choice GROUP BY question_id),
        base_text AS (SELECT a.year, a.question_id, COUNT(*) AS cnt, jsonb_agg(a.answer) FILTER (WHERE rn <= 50) AS answers 
            FROM (SELECT a.questionnaire_id, qm.year, a.question_id, BTRIM(a.answer) AS answer, ROW_NUMBER() OVER (PARTITION BY a.questionnaire_id, a.question_id, qm.year) AS rn
            FROM data.answers a JOIN input_pairs ip ON ip.questionnaire_id = a.questionnaire_id AND ip.question_id = a.question_id 
            JOIN qmeta qm ON qm.questionnaire_id = a.questionnaire_id JOIN data.questions q ON q.question_id = a.question_id
            WHERE q.question_type = 'text' AND a.answer IS NOT NULL AND BTRIM(a.answer) <> '') a GROUP BY a.year, a.question_id),
        dataset_text AS (SELECT question_id, jsonb_agg(jsonb_build_object('year', year, 'count', cnt, 'answers', answers) ORDER BY year) AS dataset 
            FROM base_text GROUP BY question_id),
        merged AS (SELECT q.question_id, q.question_text, q.question_title, q.question_type, COALESCE(dc.dataset, dt.dataset) AS dataset 
            FROM data.questions q JOIN input_pairs ip ON ip.question_id = q.question_id LEFT JOIN dataset_choice dc ON dc.question_id = q.question_id 
            LEFT JOIN dataset_text dt ON dt.question_id = q.question_id GROUP BY q.question_id, q.question_text, q.question_title, q.question_type, dc.dataset, dt.dataset)
        SELECT jsonb_build_object('question_id', question_id, 'question_Title', question_title, 'questionText', question_text,
                                  'question_type', question_type, 'dataset', dataset) AS result FROM merged ORDER BY question_id;"""

    df_7 = await db.query_sql(sql, *params)
    records = df_7.to_dict(orient="records")
    return [row["result"] for row in records]
