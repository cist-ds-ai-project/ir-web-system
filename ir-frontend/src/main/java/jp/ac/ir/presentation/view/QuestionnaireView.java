package jp.ac.ir.presentation.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.presentation.MainLayout;

@PageTitle("アンケート")
@Route(value = "questionnaire", layout = MainLayout.class)
@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
public class QuestionnaireView extends VerticalLayout {

    public QuestionnaireView() {
        addClassName("content-page");
        add(new H2("アンケート"), new Paragraph("アンケート画面です。ここに詳細画面を実装できます。"));
    }
}
