package jp.ac.ir.presentation.lecture;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.model.dto.lecture.LectureList;
import jp.ac.ir.model.dto.lecture.LectureSyllabusDetail;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import jp.ac.ir.service.lecture.LectureService;
import org.springframework.web.client.RestClientException;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("シラバス詳細")
@Route(value = "lecture/:lectureName/syllabus", layout = MainLayout.class)
@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
public class LectureSyllabusView extends VerticalLayout implements BeforeEnterObserver, BreadcrumbProvider {

    private static final LectureSyllabusDetail SAMPLE_DETAIL = new LectureSyllabusDetail(
            "c20250054",
            "高度プログラミング技法",
            1,
            "B3",
            List.of(),
            null,
            null,
            null,
            null,
            null,
            List.of(),
            List.of()
    );

    private final LectureService lectureService;
    private String lectureName;
    private String courseId;

    public LectureSyllabusView(LectureService lectureService) {
        this.lectureService = lectureService;
        addClassName("syllabus-page");
        setWidthFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        lectureName = event.getRouteParameters().get("lectureName").orElse("");
        lectureName = URLDecoder.decode(lectureName, StandardCharsets.UTF_8);
        courseId = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("courseId", List.of(""))
                .stream()
                .findFirst()
                .orElse("");
        courseId = URLDecoder.decode(courseId, StandardCharsets.UTF_8);
        buildView(resolveSyllabus(lectureName, courseId));
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        return List.of(
                BreadcrumbItem.of("科目情報", LectureSelectView.class),
                BreadcrumbItem.of(
                        lectureName,
                        LectureTopView.class,
                        new RouteParameters("lectureName", lectureName),
                        createCourseIdQueryParameters()
                ),
                BreadcrumbItem.of("シラバス詳細", LectureSyllabusView.class, new RouteParameters("lectureName", lectureName))
        );
    }

    private void buildView(LectureSyllabusDetail detail) {
        removeAll();

        VerticalLayout page = new VerticalLayout();
        page.setPadding(false);
        page.setSpacing(false);
        page.addClassName("syllabus-page__container");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.addClassName("syllabus-page__header");

        Button backButton = new Button("戻る", e -> UI.getCurrent().navigate(createLectureTopRoute()));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        H1 title = new H1("シラバス詳細");
        title.addClassName("syllabus-page__title");

        Button printButton = new Button("印刷用ページ", VaadinIcon.PRINT.create(), e -> getUI().ifPresent(ui -> ui.getPage().executeJs("window.print()")));
        printButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        printButton.addClassName("syllabus-page__print");

        header.add(backButton, title, printButton);
        header.expand(title);

        page.add(header);
        Div summaryTable = new Div();
        summaryTable.addClassName("syllabus-table");
        summaryTable.add(
                createTableRow("科目名", detail.lectureName()),
                createTableRow("科目コード", detail.courseId()),
                createTableRow("担当教員", formatTeachers(detail.teachers()))
        );
        page.add(summaryTable);

        Div metaGrid = new Div();
        metaGrid.addClassName("syllabus-meta-grid");
        metaGrid.add(
                createMetaItem("単位数", valueOrDash(detail.numberCreditsCourse())),
                createMetaItem("対象年次", detail.targetGrade()),
                createMetaItem("開講年度", deriveAcademicYear(detail.courseId()))
        );
        page.add(metaGrid);

        page.add(createSection("講義概要", detail.lectureAbout()));
        page.add(createSection("到達目標", detail.lectureGoal()));
        page.add(createSection("授業外学習", detail.studyOutsideOfClass()));
        page.add(createSection("教科書・参考資料", detail.textbook()));
        page.add(createSection("成績評価基準", detail.gradingStandardExplain()));
        page.add(createSection("授業計画", formatLecturePlans(detail.lecturePlans())));
        page.add(createSection("評価配分", formatEvaluationItems(detail.evaluationItems())));

        Scroller scroller = new Scroller(page);
        scroller.setSizeFull();
        add(scroller);
        expand(scroller);
    }

    private Component createTableRow(String label, String value) {
        Div row = new Div();
        row.addClassName("syllabus-table__row");

        Div heading = new Div();
        heading.setText(label);
        heading.addClassName("syllabus-table__heading");

        Pre content = new Pre(normalizeText(value));
        content.addClassName("syllabus-table__content");

        row.add(heading, content);
        return row;
    }

    private Component createMetaItem(String label, String value) {
        Div item = new Div();
        item.addClassName("syllabus-meta");

        Div heading = new Div();
        heading.setText(label);
        heading.addClassName("syllabus-meta__heading");

        Pre content = new Pre(normalizeText(value));
        content.addClassName("syllabus-meta__content");

        item.add(heading, content);
        return item;
    }

    private Component createTwoColumnRow(Component left, Component right) {
        HorizontalLayout row = new HorizontalLayout(left, right);
        row.setWidthFull();
        row.setSpacing(true);
        row.addClassName("syllabus-page__two-column");
        row.setFlexGrow(1, left, right);
        return row;
    }

    private Component createThreeColumnRow(Component first, Component second, Component third) {
        HorizontalLayout row = new HorizontalLayout(first, second, third);
        row.setWidthFull();
        row.setSpacing(true);
        row.addClassName("syllabus-page__three-column");
        row.setFlexGrow(1, first, second, third);
        return row;
    }

    private Component createSection(String label, String value) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.addClassName("syllabus-section");

        Div heading = new Div();
        heading.setText(label);
        heading.addClassName("syllabus-section__heading");

        Pre content = new Pre(normalizeText(value));
        content.addClassName("syllabus-section__content");

        section.add(heading, content);
        return section;
    }

    private LectureSyllabusDetail resolveSyllabus(String requestedLectureName, String requestedCourseId) {
        if (requestedCourseId != null && !requestedCourseId.isBlank()) {
            try {
                LectureSyllabusDetail detail = lectureService.getSyllabusDetailByCourseId(requestedCourseId);
                if (detail != null) {
                    return detail;
                }
            } catch (RestClientException ignored) {
                // Fall back to the lecture list so the selected course code is still reflected.
            }
        }

        LectureList selectedLecture = findSelectedLecture(requestedLectureName, requestedCourseId);
        if (selectedLecture != null) {
            return createFallbackDetail(selectedLecture);
        }

        if ((requestedLectureName == null || requestedLectureName.isBlank())
                && (requestedCourseId == null || requestedCourseId.isBlank())) {
            return SAMPLE_DETAIL;
        }

        if (SAMPLE_DETAIL.lectureName().equals(requestedLectureName)
                || SAMPLE_DETAIL.courseId().equals(requestedCourseId)) {
            return SAMPLE_DETAIL;
        }

        return new LectureSyllabusDetail(
                requestedCourseId == null || requestedCourseId.isBlank()
                        ? SAMPLE_DETAIL.courseId()
                        : requestedCourseId,
                requestedLectureName,
                SAMPLE_DETAIL.numberCreditsCourse(),
                SAMPLE_DETAIL.targetGrade(),
                SAMPLE_DETAIL.teachers(),
                SAMPLE_DETAIL.lectureAbout(),
                SAMPLE_DETAIL.lectureGoal(),
                SAMPLE_DETAIL.studyOutsideOfClass(),
                SAMPLE_DETAIL.textbook(),
                SAMPLE_DETAIL.gradingStandardExplain(),
                SAMPLE_DETAIL.lecturePlans(),
                SAMPLE_DETAIL.evaluationItems()
        );
    }

    private LectureList findSelectedLecture(String requestedLectureName, String requestedCourseId) {
        return lectureService.getLectures().stream()
                .filter(lecture -> {
                    if (requestedCourseId != null && !requestedCourseId.isBlank()) {
                        return requestedCourseId.equals(lecture.courseId());
                    }
                    return requestedLectureName != null && requestedLectureName.equals(lecture.lectureName());
                })
                .findFirst()
                .orElse(null);
    }

    private LectureSyllabusDetail createFallbackDetail(LectureList lecture) {
        return new LectureSyllabusDetail(
                lecture.courseId(),
                lecture.lectureName(),
                lecture.creditsCourse(),
                lecture.targetGrade(),
                lecture.lectureTeacher() == null || lecture.lectureTeacher().isBlank()
                        ? List.of()
                        : List.of(lecture.lectureTeacher()),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of()
        );
    }

    private String formatTeachers(List<String> teachers) {
        if (teachers == null || teachers.isEmpty()) {
            return "未設定";
        }
        return String.join("\n", teachers);
    }

    private String formatLecturePlans(List<LectureSyllabusDetail.LecturePlan> lecturePlans) {
        if (lecturePlans == null || lecturePlans.isEmpty()) {
            return "未設定";
        }
        return lecturePlans.stream()
                .sorted(Comparator.comparing(LectureSyllabusDetail.LecturePlan::lecturePlanId))
                .map(plan -> String.format("%02d. %s", plan.lecturePlanId(), plan.lecturePlan()))
                .collect(Collectors.joining("\n"));
    }

    private String formatEvaluationItems(List<LectureSyllabusDetail.EvaluationItem> evaluationItems) {
        if (evaluationItems == null || evaluationItems.isEmpty()) {
            return "未設定";
        }
        return evaluationItems.stream()
                .sorted(Comparator.comparing(LectureSyllabusDetail.EvaluationItem::evaluationItemId))
                .map(item -> String.format("%s: %d%%", item.evaluationItemTitle(), item.evaluationItemPer()))
                .collect(Collectors.joining("\n"));
    }

    private String valueOrDash(Integer value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String deriveAcademicYear(String courseId) {
        if (courseId == null || courseId.length() < 5) {
            return null;
        }

        String candidate = courseId.substring(1, 5);
        return candidate.chars().allMatch(Character::isDigit) ? candidate : null;
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return "未設定";
        }
        return value.replace("\r\n", "\n").trim();
    }

    private String encodeLectureName(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String createLectureTopRoute() {
        String route = "lecture/" + encodeLectureName(lectureName);
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
