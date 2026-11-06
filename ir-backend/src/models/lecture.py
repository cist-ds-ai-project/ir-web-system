from pydantic import BaseModel

class SelectionOptionsResponse(BaseModel):
    """選択肢のレスポンスモデル"""
    grades: list[str]        # 学年一覧
    departments: list[str]   # 学科一覧
    categories: list[str]    # 区分一覧
    semesters: list[str]     # 開講学期一覧

class LectureInfo(BaseModel):
    """科目情報のモデル"""
    course_id: str           #コースID
    lecture_name: str        # 科目名
    lecture_teacher: str     #教員名
    target_grade: str        # 対象学年
    target_department: str   # 対象学科
    category: str            # 必選別 (必修/選択)
    credits_course: int | None = None # 単位数
    available_semester: str | None = None # 学期



class LectureListResponse(BaseModel):
    """科目一覧のレスポンスモデル"""
    lectures: list[LectureInfo]