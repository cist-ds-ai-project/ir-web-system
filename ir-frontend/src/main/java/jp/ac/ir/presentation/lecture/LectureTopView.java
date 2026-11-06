package jp.ac.ir.presentation.lecture;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;

import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Route(value = "/lecture/:lectureName", layout = MainLayout.class)
@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
public class LectureTopView extends VerticalLayout implements BeforeEnterObserver, BreadcrumbProvider {

    private String lectureName;
    private String courseId;
    private final H2 title = new H2();

    public LectureTopView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Span backLink = new Span("戻る");
        backLink.getStyle()
                .set("cursor", "pointer")
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-size", "14px");

        backLink.addClickListener(e -> UI.getCurrent().navigate("grade"));

        title.getStyle()
                .set("margin-top", "8px")
                .set("margin-bottom", "16px");

        VerticalLayout menu = new VerticalLayout();
        menu.setPadding(false);
        menu.setSpacing(false);

        menu.getStyle()
                .set("max-width", "500px")
                .set("margin", "0");

        menu.add(
                createMenuItem(
                        "シラバス",
                        "講義のシラバス詳細を確認できます。",
                        () -> UI.getCurrent().navigate(createSyllabusRoute())
                ),
                createMenuItem(
                        "成績分布",
                        "成績分布を確認できます。",
                        () -> UI.getCurrent().navigate(createLectureDetailRoute("grade-detail"))
                ),
                createMenuItem(
                        "アンケート",
                        "授業評価アンケートを確認できます。",
                        () -> UI.getCurrent().navigate(createLectureDetailRoute("survey"))
                ),
                createMenuItem(
                        "履修者推移",
                        "履修者の推移を確認できます。",
                        () -> UI.getCurrent().navigate(createLectureDetailRoute("Enrollment-detail"))
                )
        );

        add(title, menu, backLink);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        lectureName = event.getRouteParameters()
                .get("lectureName")
                .orElse("");
        int queryIndex = lectureName.indexOf('?');
        if (queryIndex >= 0) {
            lectureName = lectureName.substring(0, queryIndex);
        }
        courseId = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("courseId", List.of(""))
                .stream()
                .findFirst()
                .orElse("");

        if (lectureName == null || lectureName.isBlank()) {
            title.setText("講義が指定されていません");
            return;
        }

        lectureName = java.net.URLDecoder.decode(
                lectureName,
                java.nio.charset.StandardCharsets.UTF_8
        );
        courseId = java.net.URLDecoder.decode(
                courseId,
                java.nio.charset.StandardCharsets.UTF_8
        );

        title.setText(lectureName);
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        if (lectureName == null || lectureName.isBlank()) {
            return List.of(
                    BreadcrumbItem.of("成績情報", LectureSelectView.class)
            );
        }

        return List.of(
                BreadcrumbItem.of("成績情報", LectureSelectView.class),
                BreadcrumbItem.of(
                        lectureName,
                        LectureTopView.class,
                        new RouteParameters("lectureName", lectureName),
                        createCourseIdQueryParameters()
                )
        );
    }

    private Component createMenuItem(String titleText, String description, Runnable action) {
        VerticalLayout item = new VerticalLayout();
        item.setPadding(false);
        item.setSpacing(false);

        item.getStyle()
                .set("cursor", "pointer")
                .set("padding", "12px 0")
                .set("border-bottom", "1px solid #eee");

        Span title = new Span(titleText);
        title.getStyle()
                .set("font-size", "22px")
                .set("font-weight", "600");

        Span desc = new Span(description);
        desc.getStyle()
                .set("font-size", "14px")
                .set("color", "#666");

        item.add(title, desc);
        item.addClickListener(e -> action.run());

        item.getElement().addEventListener("mouseover",
                e -> title.getStyle().set("text-decoration", "underline"));
        item.getElement().addEventListener("mouseout",
                e -> title.getStyle().set("text-decoration", "none"));

        return item;
    }

    private String createSyllabusRoute() {
        return appendCourseIdQuery("lecture/" + URLEncoder.encode(lectureName, StandardCharsets.UTF_8) + "/syllabus");
    }

    private String createLectureDetailRoute(String prefix) {
        return appendCourseIdQuery(prefix + "/" + URLEncoder.encode(lectureName, StandardCharsets.UTF_8));
    }

    private String appendCourseIdQuery(String route) {
        if (courseId != null && !courseId.isBlank()) {
            route += "?courseId=" + URLEncoder.encode(courseId, StandardCharsets.UTF_8);
        }
        return route;
    }

    private QueryParameters createCourseIdQueryParameters() {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }
        return QueryParameters.simple(Map.of("courseId", courseId));
    }
}
