package jp.ac.ir.model.entity.permission;

/**
 * データベースの authority テーブルに対応するレコード
 */
public record Authority(
        Long id,
        String authority_name
) {}
