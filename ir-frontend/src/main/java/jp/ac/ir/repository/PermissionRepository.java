package jp.ac.ir.repository;

import jp.ac.ir.model.entity.permission.Authority;
import jp.ac.ir.model.entity.permission.Role;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository("permissionRepository")
public class PermissionRepository {

    private final JdbcClient jdbcClient;

    public PermissionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * すべての権限リストを取得
     * Authorityレコードのフィールド名が authorityName の場合、AS で合わせます
     */
    public List<Authority> getAllAuthorities() {
        return jdbcClient.sql("""
                SELECT id, authority_name AS authorityName FROM users.authority
                """)
                .query(Authority.class)
                .list();
    }

    /**
     * 特定のユーザに許可されている権限IDのセットを取得
     */
    public Set<Long> findAllowedAuthorityIds(long userId) {
        List<Long> idList = jdbcClient.sql("""
                SELECT B.authority_id
                FROM
                  users.user_role A,
                  users.role_authority B
                WHERE A.user_id = ?
                AND A.role_id = B.role_id
                AND B.permission = true
                """)
                .param(userId) // RoleRepository と同じパラメータ指定方法
                .query(Long.class)
                .list();

        return idList.stream().collect(Collectors.toSet());
    }

    public Set<Long> findAllowedAuthorityIdsByRoleId(long roleId) {
        List<Long> idList = jdbcClient.sql("""
            SELECT authority_id
            FROM users.role_authority
            WHERE role_id = ?
            AND permission = true
            """)
                .param(roleId)
                .query(Long.class)
                .list();
        return idList.stream().collect(Collectors.toSet());
    }

    public List<Role> getAllRoles() {
        return jdbcClient.sql("SELECT id, role_name AS roleName FROM users.role")
                .query(Role.class)
                .list();
    }

    /**
     * 指定したロールと権限の組み合わせの permission フラグを更新する
     */
    public void updatePermission(long roleId, long authorityId, boolean allowed) {
        jdbcClient.sql("""
            UPDATE users.role_authority 
            SET permission = ? 
            WHERE role_id = ? AND authority_id = ?
            """)
                .params(allowed, roleId, authorityId)
                .update();
    }
}
