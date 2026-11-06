from pydantic import BaseModel


class DepartmentGpa(BaseModel):
    """学科ごとの平均GPAを表すモデル"""
    department_name: str
    avg_gpa: float


class DepartmentGpaResponse(BaseModel):
    """学科別平均GPA APIのレスポンスモデル"""
    data: list[DepartmentGpa]
