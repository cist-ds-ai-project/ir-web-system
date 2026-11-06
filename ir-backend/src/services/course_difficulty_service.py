from src.datasource import irweb_irweb

# 成績帯ごとの異常検知閾値（将来調整しやすいよう一箇所で管理）
THRESHOLDS = {
    "秀": {"too_high": 60.0},
    "不可": {"too_high": 20.0},
}

async def detect_alert_targets() -> dict:
    """
    全科目・全年度・全学期を対象に異常な成績分布を検知する
    複数担当教員がいる場合は教員ごとに個別のアラートを生成する
    """
    db = irweb_irweb()

    sql = """WITH grade_counts AS (
    SELECT
        c.course_id,
        c.lecture_name,
        c.available_year,
        s.semester   AS available_semester,
        p.person_id  AS teacher_id,
        p.name_last || p.name_first AS teacher_name,
        gs.grading   AS label,
        COUNT(se.student_id) AS count
    FROM data.courses c
    JOIN data.student_engagement se ON c.course_id = se.course_id
    JOIN data.grading_standards gs ON se.grading::integer = gs.id
    JOIN data.course_instructors ct ON c.course_id = ct.course_id
    JOIN data.persons p ON ct.lecture_teacher = p.person_id
    JOIN data.semesters s ON c.available_semester = s.id
    WHERE c.lecture_name IS NOT NULL
    GROUP BY
        c.course_id, c.lecture_name, c.available_year,
        s.id, s.semester, p.person_id, p.name_last, p.name_first,
        gs.id, gs.grading
)
SELECT
    *,
    count * 100.0 / NULLIF(SUM(count) OVER (PARTITION BY course_id), 0) AS percentage
FROM grade_counts
ORDER BY lecture_name, available_year, teacher_id, label
          """

    df = await db.query_sql(sql)

    if df is None or df.empty:
        return {"alerts": []}

    alerts = []

    # 科目・年度・学期・教員ごとにグループ化
    for (
        course_id,
        available_year,
        available_semester,
        teacher_id,
    ), group in df.groupby(
        ["course_id", "available_year", "available_semester", "teacher_id"]
    ):
        band_alerts = []

        for _, row in group.iterrows():
            label = row["label"]
            percentage = float(row["percentage"])

            if label not in THRESHOLDS:
                continue

            threshold = THRESHOLDS[label]

            if "too_high" in threshold and percentage >= threshold["too_high"]:
                band_alerts.append({
                    "label": label,
                    "percentage": round(percentage, 1),
                    "alert_type": "too_high",
                })
            if "too_low" in threshold and percentage <= threshold["too_low"]:
                band_alerts.append({
                    "label": label,
                    "percentage": round(percentage, 1),
                    "alert_type": "too_low",
                })

        if band_alerts:
            alerts.append({
                "course_id": course_id,
                "lecture_name": group.iloc[0]["lecture_name"],
                "teacher_name": group.iloc[0]["teacher_name"],
                "teacher_id": teacher_id,
                "available_year": int(available_year),
                "available_semester": available_semester,
                "band_alerts": band_alerts,
            })

    return {"alerts": alerts}