from pydantic import BaseModel
from typing import List, Dict

class PermissionRow(BaseModel):
    authority_id: int
    authority_name: str
    # ロールIDをキー、許可フラグを値とする辞書
    # 例: { "1": True, "2": False }
    role_permissions: Dict[int, bool]

class PermissionResponse(BaseModel):
    roles: List[Dict[str, str]] # ヘッダー用（ロール名一覧）
    permissions: List[PermissionRow] # 行データ