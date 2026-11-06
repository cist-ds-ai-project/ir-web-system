package jp.ac.ir.presentation.view;


import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.service.management.SecurityService;

// Top画面（ログイン後のホーム。未認証でのアクセスは Vaadin Security がログインへ誘導）
@PageTitle("IRTop")
@Route(value = "/top", layout = MainLayout.class)
@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
public class TopView extends VerticalLayout {

    public TopView(SecurityService securityService) {

        // 共通メニュー
        H1 menutitle1 = new H1("IR-WEBにようこそ");
        add(menutitle1);

        // 成績情報ページ（StudentView）の紹介
        H3 grade = new H3(new Anchor("lecture", "成績情報"));
        Paragraph gradeDescription = new Paragraph("成績情報を確認できます。");
        add(grade, createIndent(gradeDescription));

        // 成績評価分布状況表(未実装)
        H3 gradeGird = new H3(new Anchor("/gpa", "GPA統計"));
        Paragraph gradeGridDescription = new Paragraph("成績に関するGPAの統計情報を確認できます。");
        add(gradeGird, createIndent(gradeGridDescription));

//        // 成績評価分布状況表(未実装)
//        H3 questionnaire = new H3(new Anchor("common/grid", "アンケート"));
//        Paragraph questionnaireDescription = new Paragraph("授業評価アンケートの回答結果を確認できます。");
//        add(questionnaire, createIndent(questionnaireDescription));

        // 成績評価分布状況表(未実装)
        H3 money = new H3(new Anchor("scholarship", "奨学金"));
        Paragraph moneyDescription = new Paragraph("奨学金に関する情報を確認できます。");
        add(money, createIndent(moneyDescription));

        H3 users = new H3(new Anchor("/user_management", "ユーザー管理"));
        Paragraph usersDescription = new Paragraph("ユーザー管理を行う画面です。");
        if (securityService.hasRole("admin")) {
            add(users, createIndent(usersDescription));
        }

        H3 gradeAssess = new H3(new Anchor("teacher/courses", "担当した科目の詳細情報"));
        Paragraph gradeAssessDescription = new Paragraph("担当した科目の詳細情報を確認できます。");
        if (securityService.hasRole("teacher")) {
            add(gradeAssess, createIndent(gradeAssessDescription));
        }

        //H3 studentAssess = new H3(new Anchor("/grade/student", "履修した科目の詳細情報"));
        //Paragraph studentAssessDescription = new Paragraph("履修した科目の詳細情報を確認できます");
        //add(studentAssess, createIndent(studentAssessDescription));

        // 個人成績ページ
        H3 personalGrade = new H3(new Anchor("personal", "個人成績"));
        Paragraph personalGradeDescription = new Paragraph("個人の成績や出席状況を確認できます。");
        if (securityService.hasRole("student")) {
            add(personalGrade, createIndent(personalGradeDescription));
        }

    }


    // paragraphにインデントを付ける
    private Paragraph createIndent(Paragraph paragraph) {
        paragraph.getStyle().set("margin-left", "2em");
        return paragraph;
    }
}
