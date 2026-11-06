import pandas as pd
from fastapi import APIRouter, Depends
from src.datasource import irweb_irweb
import json
from collections import Counter

router = APIRouter()

#GPAに関するグラフを生成するための情報を取得
@router.get("/grade/grade_graph/{course_id}")
async def subject(course_id: str, db_connection=Depends(irweb_irweb)):
    df = await db_connection.query("grade_new","grading", "lecture_name", "lecture_teacher" ,where_and={"course_id":course_id})
    # Counterクラスを使ってカウント(importを忘れないように)
    count_dict = dict(Counter(df["grading"].values))
    count_dict["教員名"] = df["lecture_teacher"][0]
    count_dict["科目名"] = df["lecture_name"][0]
    print({"data": count_dict})
    return {"data": count_dict}

@router.get("/grade/subject/{student_number}")
async def student_number_grades(student_number: str, db_connection=Depends(irweb_data)):
    query = """
        SELECT
            DISTINCT current.course_id AS course_id,
            current.account_id AS account_id,
            current.lecture_name AS lecture_name,
            current.grading AS grading,
            current.target_grade AS target_grade,
            current.target_department AS target_department,
            current.compulsory_subjects AS compulsory_subjects,
            current.number_credits_course AS number_credits_course,
            previous.course_id AS pre_year_course_id
        FROM
            grade_new AS current
        LEFT JOIN
            grade_new AS previous
        ON
            previous.lecture_name = current.lecture_name
            AND previous.available_year = current.available_year - 1
        WHERE
            current.student_id = %s
    """
    df = await db_connection.query_sql(query, (student_number,))
    print({"data": json.loads(df.to_json(orient='records'))})
    return {"data": json.loads(df.to_json(orient="records"))}

#目標設定に関するデータの取得(account_idは学籍番号、course_idは授業ごとに付けられた番号)
@router.get("/grade/target/{account_id}/{course_id}")
async def subject(account_id: str, course_id: str, db_connection=Depends(irweb_data)):
    query = """
        SELECT
            target.question_1 AS target_question_1,
            target.answer_1_1 AS target_answer_1,
            review.question_1 AS review_question_1,
            review.answer_1_1 AS review_answer_1
        FROM
            target
        INNER JOIN review_new AS review
            ON target.account_id = review.account_id
        WHERE
            target.account_id = %s AND target.course_id = %s
    """
    df = await db_connection.query_sql(query, account_id, course_id)
    if df.empty:
        df1 = await db_connection.query("review_new", "DISTINCT(question_1)", as_name={"DISTINCT(question_1)": "review_question_1"})
        df2 = await db_connection.query("target", "DISTINCT(question_1)", as_name={"DISTINCT(question_1)": "target_question_1"})
        df = pd.concat([df1, df2, pd.DataFrame({"target_answer_1": ["なし"], "review_answer_1": ["なし"]})], axis=1)
        print({"data":json.loads(df.to_json(orient="records"))})
        return {"data":json.loads(df.to_json(orient="records"))}
    else:
        return {"data":json.loads(df.to_json(orient="records"))}

@router.get("/grade/subject")
async def subject(db_connection=Depends(irweb_data)):
    df = await db_connection.query("grade_new", "available_year", "lecture_name", "target_grade", "target_department", "compulsory_subjects", "number_credits_course")
    df.dropna(inplace=True)
    df.drop_duplicates(subset="lecture_name", inplace=True)
    print({"data":df.to_dict(orient="records")})
    return {"data": json.loads(df.to_json(orient="records"))}

@router.get("/grade/subjectAll")
async def subject_all(db_connection=Depends(irweb_data)):
    df = await db_connection.query("grade_new", "available_year", "lecture_name", "target_grade", "target_department", "compulsory_subjects", "number_credits_course", "course_id")
    df.dropna(inplace=True)
    df.drop_duplicates(subset=["lecture_name", "available_year"], inplace=True)
    print({"data":df.to_dict(orient="records")})
    return {"data":json.loads(df.to_json(orient="records"))}

@router.get("/grade/courseIds")
async def subject(db_connection=Depends(irweb_data)):
    df = await db_connection.query("grade_new", "available_year", "lecture_name", "course_id", "target_grade", "target_department", "compulsory_subjects", "number_credits_course")
    df.dropna(inplace=True)
    lecture_names = df["lecture_name"].unique()
    course_id_dict = {}
    for lecture_name in lecture_names:
        course_id_dict[lecture_name] = {}
        for i in df[df["lecture_name"] == lecture_name].drop_duplicates(subset=["lecture_name", "course_id"]).index:
            course_id_dict[lecture_name][str(df["available_year"].iloc[i])] = df["course_id"].iloc[i]
    print(course_id_dict)
    return {"courseIdDict": course_id_dict}

