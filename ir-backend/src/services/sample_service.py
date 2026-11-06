# # from src.datasource import irweb_data
#
#
# async def get_department_avg_gpa() -> list[dict]:
#     """学科別の平均GPAを取得する
#
#     Returns:
#         [{"department": "情報", "avg_gpa": 2.8}, ...] 形式のリスト
#     """
#     # db = irweb_data()
#
#
#     df = await db.query_sql("""
#         SELECT department_name, AVG(gpa) AS avg_gpa
#         FROM (
#             SELECT DISTINCT student_id, department_name, gpa
#             FROM grade
#         ) AS unique_students
#         GROUP BY department_name
#     """)
#
#     # department_name が NULL のレコードを除外
#     df = df.dropna(subset=["department_name"])
#
#     return df.to_dict(orient="records")
