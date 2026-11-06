from pydantic import BaseModel
from typing import List, Optional


class GpaFilter(BaseModel):
    """検索条件"""
    years: Optional[List[int]] = None
    faculty: Optional[str] = None
    departments: Optional[List[str]] = None
    grades: Optional[List[str]] = None


class GpaSelectionOptionsResponse(BaseModel):
    """GPAページ用の選択肢レスポンス"""
    years: List[int]        # 年度一覧
    faculties: List[str]    # 学部一覧
    departments: List[str]  # 学科一覧
    grades: List[str]       # 学年一覧


class GpaRawResponse(BaseModel):
    """ヒストグラム用データ"""
    gpas: List[float]


class DepartmentGpa(BaseModel):
    """学科ごとのGPA配列"""
    department: str
    gpas: List[float]


class DepartmentGpaResponse(BaseModel):
    """学科別GPAレスポンス"""
    departments: List[DepartmentGpa]


class GpaStatistics(BaseModel):
    """統計情報"""
    count: int
    avg: float
    min: float
    max: float
    std: float


class DepartmentStatistics(BaseModel):
    """学科ごとの統計"""
    department: str
    stats: GpaStatistics


class GpaStatsResponse(BaseModel):
    """全体＋学科別統計レスポンス"""
    overall: GpaStatistics
    by_department: List[DepartmentStatistics]