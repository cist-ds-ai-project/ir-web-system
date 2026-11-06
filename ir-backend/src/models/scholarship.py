from pydantic import BaseModel
from typing import List, Dict, Any

class ScholarshipDistributionResponse(BaseModel):
    labels: List[str]
    data: List[int]
    total: int

class ScholarshipTrendResponse(BaseModel):
    data: List[Dict[str, Any]]
    lines: List[str]

from pydantic import BaseModel
from typing import List


class ScholarshipDistributionResponse(BaseModel):
    labels: List[str]
    data: List[int]
    total: int

