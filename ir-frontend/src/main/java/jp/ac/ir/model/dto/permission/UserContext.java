package jp.ac.ir.model.dto.permission;

import java.util.Set;

/**
 * ログイン中のユーザー情報をフロントエンドで保持するクラス
 */
public record UserContext(
        String loginId,
        String userName,
        String roleName,
        String roleDisplayName,
        Set<Long> allowedAuthorityIds
) {}