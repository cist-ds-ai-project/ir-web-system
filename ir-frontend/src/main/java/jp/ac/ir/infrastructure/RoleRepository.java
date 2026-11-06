package jp.ac.ir.infrastructure;

import jp.ac.ir.service.management.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("infraRoleRepository")
public class RoleRepository {
    // フィールド；JDBC Client
    @Autowired
    private JdbcClient jdbcClient;

    // コンストラクタ
    public RoleRepository(JdbcClient jdbcClient){
        this.jdbcClient = jdbcClient;
    }

    // user_idに応じたロールのリストを取得
    public List<Role> getAllRolesList(){
        List<Role> roleList = jdbcClient.sql("""
                SELECT id, role_name AS name, display_name FROM users.role
                """)
                .query(Role.class)
                .list();
        return roleList;
    }

    // 特定のユーザのロールのリストを取得
    public List<Role> getRoleList(long userId){
        List<Role> roleList = jdbcClient.sql("""
                SELECT B.id, B.role_name AS name, B.display_name
                FROM
                  users.user_role A,
                  users.role B
                WHERE A.user_id = ?
                AND A.role_id = B.id
                """)
                .param(userId)
                .query(Role.class)
                .list();
        return roleList;
    }

    // roleの名前(display_name)からrole_idを取得
    public Optional<Integer> getRoleId(String roleName){
        Optional<Integer> roleIdOp = jdbcClient.sql("""
                SELECT id FROM users.role WHERE display_name = ?
                """)
                .params(roleName)
                .query(Integer.class)
                .optional();
        return roleIdOp;
    }

    // roleのリストを取得
    public List<String> getRole(){
        List<String> roleList = jdbcClient.sql("""
            SELECT display_name FROM users.role
            """)
            .query(String.class)
            .list();
        return roleList;
    }

}
