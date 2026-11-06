package jp.ac.ir.presentation.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.presentation.MainLayout;

@PageTitle("Teacher")
@Route(value = "teacher", layout = MainLayout.class)
@RolesAllowed({"admin", "teacher", "ir_committee"})
public class TeacherView extends VerticalLayout {

    public TeacherView() {
        addClassName("content-page");
        add(new H2("Teacher"), new Paragraph("教員向け画面です。ここに詳細画面を実装できます。"));
    }
}
