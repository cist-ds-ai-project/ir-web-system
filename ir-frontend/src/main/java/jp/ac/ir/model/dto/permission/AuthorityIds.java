package jp.ac.ir.model.dto.permission;

/**
 * DBの authority_id とプログラム上の機能を紐付ける定数
 */
public class AuthorityIds {
    // ユーザ管理系
    public static final Long CREATE_USER = 1L;
    public static final Long DELETE_USER = 2L;
    public static final Long EDIT_USER = 3L;

    // 成績系
    public static final Long VIEW_GRADES_OWN = 4L;      // 担当/履修
    public static final Long VIEW_GRADES_OTHERS = 5L;   // 非担当/未履修

    // アンケート系
    public static final Long VIEW_SURVEY_SUMMARY = 8L;  // 授業評価アンケート概要
    public static final Long VIEW_IR_SURVEY_DETAIL = 13L; // IRアンケート詳細
}