from src.datasource import irweb_irweb
import pandas as pd


async def get_permission_matrix() -> dict:
    """機能管理マトリックス（機能×ロールの権限一覧）を取得する"""
    db = irweb_irweb()

    # ロール一覧を取得
    df_roles = await db.query_sql("SELECT id, display_name FROM users.role ORDER BY id")

    # idを文字列に変換
    roles_list = []
    for _, row in df_roles.iterrows():
        roles_list.append({
            "id": str(row["id"]),
            "name": row["display_name"]
        })

    # 権限とロールの全組み合わせを取得
    df_matrix = await db.query_sql("""
                                   SELECT a.id                           AS authority_id,
                                          a.authority_name,
                                          r.id                           AS role_id,
                                          COALESCE(ra.permission, false) AS is_permitted
                                   FROM users.authority a
                                            CROSS JOIN users.role r
                                            LEFT JOIN users.role_authority ra
                                                      ON a.id = ra.authority_id AND r.id = ra.role_id
                                   ORDER BY a.id, r.id
                                   """)

    permissions_list = []

    # pandas を使って「横持ち（マトリックス形式）」に変換
    # authority_id ごとにグループ化
    for auth_id, group in df_matrix.groupby("authority_id"):
        auth_name = group["authority_name"].iloc[0]

        # role_id をキー、is_permitted を値とした辞書を作成
        role_permissions = {
            str(row["role_id"]): bool(row["is_permitted"])
            for _, row in group.iterrows()
        }

        permissions_list.append({
            "authority_id": int(auth_id),
            "authority_name": auth_name,
            "role_permissions": role_permissions
        })

    return {
        "roles": roles_list,
        "permissions": permissions_list
    }


async def update_permission(authority_id: int, role_id: int, permission: bool):
    """
    指定された権限とロールの組み合わせを更新する
    """
    db = irweb_irweb()

    # PostgreSQLのUPSERT構文を使用
    # role_id と authority_id の組み合わせが既にあったら permission だけ更新する
    sql = """
          INSERT INTO users.role_authority (role_id, authority_id, permission)
          VALUES (:role_id, :authority_id, :permission) ON CONFLICT (role_id, authority_id)
        DO \
          UPDATE SET permission = EXCLUDED.permission \
          """

    params = {
        "role_id": role_id,
        "authority_id": authority_id,
        "permission": permission
    }

    # 参考コードの作法に合わせ、クエリを実行
    # (更新系なので戻り値は基本不要。db側にexecute_sqlのようなメソッドがあればそれを使う)
    await db.execute_sql(sql, params)