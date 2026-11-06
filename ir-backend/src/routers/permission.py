from fastapi import APIRouter
from src.services import permission_service

router = APIRouter(prefix="/permissions", tags=["permissions"])

@router.get("")
async def get_permissions():
    """機能管理マトリックスを取得するAPI"""
    return await permission_service.get_permission_matrix()

@router.put("/update")
async def update_permission(authority_id: int, role_id: int, permission: bool):
    """特定の権限を更新するAPI"""
    await permission_service.update_permission(authority_id, role_id, permission)
    return {"status": "success"}