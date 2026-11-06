import pandas as pd
from starlette.exceptions import HTTPException
from src.datasource import irweb_irweb

_gp_master_cache = None

async def get_gp_master() -> dict:
    global _gp_master_cache

    if _gp_master_cache is not None:
        return _gp_master_cache

    db = irweb_irweb()
    sql = """SELECT 
                 id,
                 grading,
                 gp 
             FROM data.grading_standards
          """

    df = await db.query_sql(sql)

    if df is None or df.empty:
        raise HTTPException(status_code=500, detail="GPマスタが取得できませんでした")

    _gp_master_cache = {
        "label_to_gp": dict(zip(df["grading"], df["gp"])),
        "id_to_label": dict(zip(df["id"], df["grading"])),
        "id_to_gp": dict(zip(df["id"], df["gp"])),
        "display_order": df["grading"].tolist()
    }

    return _gp_master_cache



async def get_categories() -> list[dict]:
    db = irweb_irweb()
    sql_categoies = """SELECT 
                id, 
                category_name
             FROM data.course_categories"""


    df_categories = await db.query_sql(sql_categoies)

    if df_categories is None or df_categories.empty:
        return []

    return [
        {"value": row["id"], "label": row["category_name"]}
        for _, row in df_categories.iterrows()
    ]

async def get_lecture_distribution(lecture_name: str) -> dict:
    master = await get_gp_master()
    db = irweb_irweb()

    sql_grades = """
          SELECT
              c.available_year,
              se.grading AS grading_id
          FROM data.courses c
          JOIN data.student_engagement se ON c.course_id = se.course_id
          WHERE TRIM(c.lecture_name) = %s
          """

    sql_teachers = """
                   SELECT c.available_year, 
                          p.name_last || p.name_first AS lecture_teacher
                   FROM data.courses c
                            JOIN data.course_instructors ct ON c.course_id = ct.course_id
                            JOIN data.persons p ON ct.lecture_teacher = p.person_id
                   WHERE TRIM(c.lecture_name) = %s 
                   """

    df_grades = await db.query_sql(sql_grades, lecture_name.strip())
    df_teachers = await db.query_sql(sql_teachers, lecture_name.strip())

    if df_grades is None or df_grades.empty:
        return {"lecture_name": lecture_name, "distributions": []}

    results = []

    for year, group in df_grades.groupby("available_year"):
        ids = pd.to_numeric(group["grading_id"], errors='coerce').dropna().astype(int)

        labels = ids.map(master["id_to_label"])
        gp_values = ids.map(master["id_to_gp"])

        actual_counts = labels.value_counts().to_dict()
        counts = {label: int(actual_counts.get(label, 0)) for label in master["display_order"]}

        # 教員名を年度で絞って取得
        if df_teachers is not None and not df_teachers.empty:
            teachers = (
                df_teachers[df_teachers["available_year"] == year]["lecture_teacher"]
                .dropna().unique().tolist()
            )
            teacher_str = "、".join(teachers) if teachers else "不明"
        else:
            teacher_str = "不明"

        if len(ids) > 0:
            percentages = {
                label: round((count / len(ids)) * 100, 2)
                for label, count in counts.items()
            }
        else:
            percentages = {
                label: 0.0
                for label in counts.keys()
            }

        results.append({
            "year": int(year),
            "lecture_teacher": teacher_str,
            "counts": counts,
            "total_students": len(ids),
            "percentages": percentages,
            "average": round(float(gp_values.mean()), 2) if not gp_values.empty else 0.0,
            "variance": round(float(gp_values.var(ddof=0)), 2) if len(gp_values) > 1 else 0.0
        })

    return {
        "lecture_name": lecture_name,
        "distributions": sorted(results, key=lambda x: x["year"], reverse=True)
    }