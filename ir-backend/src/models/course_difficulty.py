from pydantic import BaseModel


class GradeBandAlert(BaseModel):
    """成績帯ごとの異常情報"""
    label: str          # 異常が検知された成績帯（例: "秀", "不可"）
    percentage: float   # 実際の割合（%）
    alert_type: str     # 異常の種別（"too_high" / "too_low"）


class AlertTarget(BaseModel):
    """異常検知で引っかかった科目"""
    course_id: str
    lecture_name: str
    teacher_name: str
    teacher_id: str                    # 通知テーブル保存時に使用
    available_year: int
    available_semester: str            # 前期・後期の区別
    band_alerts: list[GradeBandAlert]  # 異常が検知された成績帯（複数あり得る）


class AlertTargetListResponse(BaseModel):
    """異常検知結果一覧"""
    alerts: list[AlertTarget]