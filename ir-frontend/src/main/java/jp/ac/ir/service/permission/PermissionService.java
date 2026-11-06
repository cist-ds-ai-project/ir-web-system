package jp.ac.ir.service.permission;

import jp.ac.ir.model.dto.permission.UserContext;
import jp.ac.ir.model.entity.permission.Authority;
import jp.ac.ir.model.entity.permission.Role;
import jp.ac.ir.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private UserContext currentUser;
    private static final String ADMIN_ROLE = "admin";

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * 指定された権限IDを持っているかチェックする
     * 日本語を使わず、AuthorityIdsクラスの定数（ID）で判定します。
     */
    public boolean hasAuthority(Long authorityId) {
        if (currentUser == null) return false;

        if (ADMIN_ROLE.equals(currentUser.roleName())) {
            return true;
        }

        return currentUser.allowedAuthorityIds().contains(authorityId);
    }

    /**
     * ログイン成功時に呼び出し、ユーザーの権限情報をDBから取得して保持する
     */
    public void setUserContext(long userId, String userName, String roleName, String roleDisplayName) {
        // Repositoryから許可されたIDのセットを取得
        Set<Long> allowedIds = permissionRepository.findAllowedAuthorityIds(userId);

        this.currentUser = new UserContext(
                String.valueOf(userId),
                userName,
                roleName,
                roleDisplayName,
                allowedIds
        );
    }

    public UserContext getCurrentUser() {
        return currentUser;
    }

    /**
     * 画面からの要求を受けてDBを更新する
     */
    public void updateRolePermission(long roleId, long authorityId, boolean isAllowed) {
        permissionRepository.updatePermission(roleId, authorityId, isAllowed);
    }

    /**
     * 管理画面の表示用に、全権限リストを取得する
     */
    public List<Authority> getAllAuthorities() {
        return permissionRepository.getAllAuthorities();
    }

    public Set<Long> getAllowedAuthorityIdsByRoleId(long roleId) {
        return permissionRepository.findAllowedAuthorityIdsByRoleId(roleId);
    }

    /**
     * 管理画面のコンボボックス表示用に、全ロールリストを取得する
     */
    public List<Role> getAllRoles() {
        return permissionRepository.getAllRoles();
    }
}
