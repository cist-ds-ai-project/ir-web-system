from pydantic import BaseModel


class TeacherInfo(BaseModel):
    """担当教員情報のモデル"""
    person_id: str          # 教員ID
    teacher_name: str       # 教員名
    department_name: str | None = None  # 所属
    is_responsible: bool = False        # 責任教員かどうか(表示側で強調する場合に使用)


class EvaluationItem(BaseModel):
    """評価項目のモデル(成績評価基準を割合形式で表示する場合)"""
    evaluation_item_id: int
    evaluation_item_title: str
    evaluation_item_per: int | None = None  # 割合(%)


class LecturePlan(BaseModel):
    """授業計画（回ごと）のモデル"""
    lecture_plan_id: int
    lecture_plan: str | None = None


class SyllabusDetailResponse(BaseModel):
    """シラバス詳細画面のレスポンスモデル"""
    # --- 科目基本情報 (courses) ---
    course_id: str
    lecture_name: str                         # 科目名
    number_credits_course: int | None = None  # 単位数
    target_grade: str | None = None           # 対象年次
    # 授業形態: courses側に対応する列がまだ無いため未実装。列が追加され次第ここに追加する。

    # --- 担当教員 ---
    teachers: list[TeacherInfo] = []

    # --- シラバス本文 (syllabus) ---
    lecture_about: str | None = None            # 授業の概要
    lecture_goal: str | None = None             # 到達目標
    study_outside_of_class: str | None = None   # 授業外学習について
    textbook: str | None = None                 # テキスト・教科書等
    grading_standard_explain: str | None = None # 成績評価の基準(文章で説明する場合)

    # --- 授業計画・成績評価の割合(表がある場合) ---
    lecture_plans: list[LecturePlan] = []
    evaluation_items: list[EvaluationItem] = []
