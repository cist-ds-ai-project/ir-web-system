from pydantic import BaseModel
from typing import Dict


class YearlyDistribution(BaseModel):
    """1科目分の成績分布のモデル"""
    year: int                # 年度
    lecture_teacher: str     # 担当教員
    counts: Dict[str, int]   # 成績帯の人数
    total_students: int      # 履修者数
    percentages: Dict[str, float]  # 百分率表示用
    average: float           # 平均
    variance: float          # 分散



class GradeDetailResponse(BaseModel):
    """1科目分のレスポンスモデル"""
    lecture_name: str        # 科目名
    distributions: list[YearlyDistribution]  # 成績分布



class GradeStandard(BaseModel):
    """GPのレスポンスモデル"""
    grading: str
    gp: float



class CategoryItem(BaseModel):
    """1つ分の必修選択モデル"""
    value: int
    label: str



class CategoryListResponse(BaseModel):
    """必修選択のレスポンスモデル"""
    categories: list[CategoryItem]