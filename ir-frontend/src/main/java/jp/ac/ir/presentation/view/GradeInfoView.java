package jp.ac.ir.presentation.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.presentation.MainLayout;

@PageTitle("成績情報")
@Route(value = "grade-info", layout = MainLayout.class)
@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
public class GradeInfoView extends VerticalLayout {

    public GradeInfoView() {
        addClassName("content-page");
        add(new H2("成績情報"), new Paragraph("成績情報画面です。ここに詳細画面を実装できます。"));
    }
}
