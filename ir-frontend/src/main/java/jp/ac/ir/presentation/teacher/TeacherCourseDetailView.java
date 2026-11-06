package jp.ac.ir.presentation.teacher;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jp.ac.ir.model.dto.grade.GradeDetailResponse;
import jp.ac.ir.model.dto.grade.YearlyDistribution;
import jp.ac.ir.model.dto.teacher.TeacherCourseInfo;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.grade.UnifiedChart;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import jp.ac.ir.service.grade.GradeService;
import jp.ac.ir.service.teacher.TeacherCourseService;

import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@PageTitle("授業詳細")
@Route(value = "teacher/courses/:courseId", layout = MainLayout.class)
@RolesAllowed("teacher")
@JsModule("./components/unified-chart.tsx")
public class TeacherCourseDetailView extends VerticalLayout
        implements BeforeEnterObserver, BreadcrumbProvider {

    private final TeacherCourseService teacherCourseService;
    private final GradeService gradeService;

    private final VerticalLayout content = new VerticalLayout();

    private String courseId;
    public TeacherCourseDetailView(
            TeacherCourseService teacherCourseService,
            GradeService gradeService
    ) {
        this.teacherCourseService = teacherCourseService;
        this.gradeService = gradeService;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        content.setWidthFull();
        content.setPadding(false);
        content.setSpacing(true);

        add(content);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        this.courseId = event.getRouteParameters()
                .get("courseId")
                .orElse(null);

        if (courseId == null || courseId.isBlank()) {
            return;
        }

        TeacherCourseInfo course = teacherCourseService.getLoginTeacherCourse(courseId);

        if (course == null) {
            content.removeAll();

            content.add(new H1("授業が見つかりません"));

            return;
        }

        showCourse(course);
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {

        if (courseId == null || courseId.isBlank()) {
            return List.of(
                    BreadcrumbItem.of("授業情報", TeacherCourseView.class),
                    BreadcrumbItem.of("授業詳細", TeacherCourseDetailView.class)
            );
        }

        TeacherCourseInfo course = teacherCourseService.getLoginTeacherCourse(courseId);

        if (course == null) {
            return List.of(
                    BreadcrumbItem.of("授業情報", TeacherCourseView.class),
                    BreadcrumbItem.of("授業詳細", TeacherCourseDetailView.class)
            );
        }

        return List.of(
                BreadcrumbItem.of(
                        "授業情報",
                        TeacherCourseView.class
                ),
                BreadcrumbItem.of(
                        course.lectureName(),
                        TeacherCourseDetailView.class,
                        new RouteParameters(
                                "courseId",
                                course.courseId()
                        )
                ),
                BreadcrumbItem.of(
                        "授業詳細",
                        TeacherCourseDetailView.class,
                        new RouteParameters(
                                "courseId",
                                course.courseId()
                        )
                )
        );
    }

    private void showCourse(TeacherCourseInfo course) {

        content.removeAll();

        H1 title = new H1(course.lectureName());

        content.add(title);

        showCourseInfo(course);

        showGradeDistribution(course);
    }

    private void showCourseInfo(TeacherCourseInfo course) {

        H2 title = new H2("授業情報");

        Grid<TeacherCourseInfo> grid = new Grid<>(TeacherCourseInfo.class, false);

        grid.addColumn(TeacherCourseInfo::courseId)
                .setHeader("科目ID")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(TeacherCourseInfo::lectureName)
                .setHeader("授業名")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(TeacherCourseInfo::year)
                .setHeader("年度")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(TeacherCourseInfo::semester)
                .setHeader("学期")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(TeacherCourseInfo::category)
                .setHeader("区分")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(TeacherCourseInfo::creditsCourse)
                .setHeader("単位数")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(TeacherCourseInfo::studentCount)
                .setHeader("受講人数")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setItems(course);

        grid.setWidth("fit-content");
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        content.add(title, grid);
    }

    private void showGradeDistribution(TeacherCourseInfo course) {

        H2 title = new H2("成績分布");
        GradeDetailResponse response = gradeService.getLectureDetail(course.lectureName());

        if (response == null || response.distributions() == null || response.distributions().isEmpty()) {

            content.add(title);

            return;
        }
        YearlyDistribution distribution =
                response.distributions()
                        .stream()
                        .max(
                                Comparator.comparingInt(
                                        YearlyDistribution::year
                                )
                        )
                        .orElse(null);

        if (distribution == null) {

            distribution = response.distributions().get(response.distributions().size() - 1);
        }

        if (distribution.counts() == null || distribution.counts().isEmpty()) {

            content.add(title);

            return;
        }

        List<String> gradeLabels = new ArrayList<>(distribution.counts().keySet());

        int maxValue =
                distribution.counts()
                        .values()
                        .stream()
                        .mapToInt(Integer::intValue)
                        .max()
                        .orElse(0);

        int niceMax = roundUpNice(maxValue);

        UnifiedChart chart = new UnifiedChart();

        chart.setWidthFull();
        chart.setHeight("260px");

        chart.setGradeOrder(gradeLabels);

        chart.setMaxValue(niceMax);

        chart.setChartData(distribution.counts());

        chart.setChartType("bar");

        Grid<Map<String, Object>> summaryGrid = createCountSummaryGrid(gradeLabels);

        summaryGrid.setItems(List.of(toCountSummaryMap(distribution, gradeLabels)));

        content.add(title, chart, summaryGrid);
    }

    private Grid<Map<String, Object>> createCountSummaryGrid(List<String> gradeLabels) {

        Grid<Map<String, Object>> grid = new Grid<>();

        grid.addColumn(row -> row.get("totalStudents"))
                .setHeader("受講人数")
                .setWidth("110px")
                .setFlexGrow(0);


        for (String label : gradeLabels) {

            grid.addColumn(row -> row.get(label))
                    .setHeader(label)
                    .setAutoWidth(false)
                    .setFlexGrow(1);
        }

        grid.addColumn(row -> row.get("average"))
                .setHeader("平均")
                .setWidth("80px")
                .setFlexGrow(0);

        grid.addColumn(row -> row.get("variance"))
                .setHeader("分散")
                .setWidth("80px")
                .setFlexGrow(0);

        grid.setWidthFull();

        grid.setAllRowsVisible(true);

        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        grid.getStyle().set("font-size", "0.85rem");

        return grid;
    }

    private Map<String, Object> toCountSummaryMap(YearlyDistribution distribution, List<String> gradeLabels) {

        Map<String, Object> row = new LinkedHashMap<>();

        row.put("totalStudents", distribution.totalStudents() + "人");

        for (String label : gradeLabels) {

            int count = distribution.counts().getOrDefault(label, 0);

            row.put(label, count + "人");
        }
        row.put("average", distribution.average());

        row.put("variance", distribution.variance());

        return row;
    }

    private int roundUpNice(int value) {

        if (value <= 10) {
            return 10;
        }

        if (value <= 50) {
            return ((value + 9) / 10) * 10;
        }

        if (value <= 100) {
            return ((value + 9) / 10) * 10;
        }

        if (value <= 500) {
            return ((value + 49) / 50) * 50;
        }

        return ((value + 99) / 100) * 100;
    }
}
