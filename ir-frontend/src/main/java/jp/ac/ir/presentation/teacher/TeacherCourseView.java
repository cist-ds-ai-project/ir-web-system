package jp.ac.ir.presentation.teacher;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.model.dto.teacher.TeacherCourseInfo;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import jp.ac.ir.service.teacher.TeacherCourseService;

import java.util.List;

@Route(value = "teacher/courses", layout = MainLayout.class)
@RolesAllowed("teacher")
@PageTitle("担当授業")
public class TeacherCourseView extends VerticalLayout implements BreadcrumbProvider {

    private final TeacherCourseService teacherCourseService;

    private List<TeacherCourseInfo> allCourses;

    private final RadioButtonGroup<String> yearGroup = new RadioButtonGroup<>();

    private final RadioButtonGroup<String> semesterGroup = new RadioButtonGroup<>();

    private final RadioButtonGroup<String> categoryGroup = new RadioButtonGroup<>();

    private final TextField lectureSearch = new TextField("授業検索");

    private final Grid<TeacherCourseInfo> grid = new Grid<>(TeacherCourseInfo.class, false);

    public TeacherCourseView(
            TeacherCourseService teacherCourseService
    ) {
        this.teacherCourseService = teacherCourseService;

        setSizeFull();

        createHeader();
        createFilterArea();
        createGrid();

        loadCourses();
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        return List.of(
                BreadcrumbItem.of(
                        "授業情報",
                        TeacherCourseView.class
                )
        );
    }


    private void createHeader() {

        H1 title = new H1("担当授業");

        add(title);
    }


    private void createFilterArea() {

        yearGroup.setLabel("年度");
        yearGroup.addValueChangeListener(event -> refreshGrid());

        semesterGroup.setLabel("学期");
        semesterGroup.addValueChangeListener(event -> refreshGrid());

        categoryGroup.setLabel("区分");
        categoryGroup.addValueChangeListener(event -> refreshGrid());

        lectureSearch.setLabel("授業検索");
        lectureSearch.setPlaceholder("授業名を入力");
        lectureSearch.setClearButtonVisible(true);
        lectureSearch.setWidth("250px");
        lectureSearch.addValueChangeListener(event -> refreshGrid());

        // 年度
        HorizontalLayout yearRow = new HorizontalLayout(yearGroup);
        yearRow.setWidthFull();
        yearRow.setAlignItems(Alignment.CENTER);

        // 学期
        HorizontalLayout semesterRow = new HorizontalLayout(semesterGroup);
        semesterRow.setWidthFull();
        semesterRow.setAlignItems(Alignment.CENTER);

        // 区分
        HorizontalLayout categoryRow = new HorizontalLayout(categoryGroup);
        categoryRow.setWidthFull();
        categoryRow.setAlignItems(Alignment.CENTER);

        // 授業検索
        HorizontalLayout searchRow = new HorizontalLayout(lectureSearch);
        searchRow.setWidthFull();
        searchRow.setAlignItems(Alignment.CENTER);

        VerticalLayout filterArea = new VerticalLayout(
                yearRow,
                semesterRow,
                categoryRow,
                searchRow
        );

        filterArea.setPadding(false);
        filterArea.setSpacing(false);
        filterArea.setWidthFull();

        add(filterArea);
    }

    private void loadCourses() {

        allCourses = teacherCourseService.getLoginTeacherCourses();

        createFilterOptions();

        refreshGrid();
    }

    private void createFilterOptions() {

        yearGroup.setItems(teacherCourseService.getDisplayYears(allCourses));
        yearGroup.setValue("全年度");

        semesterGroup.setItems(teacherCourseService.getDisplaySemesters(allCourses));
        semesterGroup.setValue("全体");


        categoryGroup.setItems(teacherCourseService.getDisplayCategories(allCourses));
        categoryGroup.setValue("全体");
    }

    private void refreshGrid() {

        if (allCourses == null) {
            return;
        }

        List<TeacherCourseInfo> courses = teacherCourseService.getCoursesFiltered(
                        allCourses,
                        yearGroup.getValue(),
                        semesterGroup.getValue(),
                        categoryGroup.getValue(),
                        lectureSearch.getValue()
                );

        grid.setItems(courses);
    }

    private void createGrid() {


        grid.addComponentColumn(course -> {
                    Span name = new Span(course.lectureName());
                    name.getStyle().set("color", "black");
                    name.getStyle().set("cursor", "pointer");

                    name.addClickListener(event -> {
                        UI.getCurrent().navigate(
                                TeacherCourseDetailView.class,
                                new RouteParameters("courseId", course.courseId())
                        );
                    });

                    return name;
                })
                .setHeader("授業名")
                .setAutoWidth(true);

        grid.addColumn(TeacherCourseInfo::year)
                .setHeader("年度")
                .setAutoWidth(true);

        grid.addColumn(TeacherCourseInfo::semester)
                .setHeader("学期")
                .setAutoWidth(true);

        grid.addColumn(TeacherCourseInfo::category)
                .setHeader("区分")
                .setAutoWidth(true);

        grid.addColumn(TeacherCourseInfo::creditsCourse)
                .setHeader("単位")
                .setAutoWidth(true);

        grid.addColumn(TeacherCourseInfo::studentCount)
                .setHeader("履修者数")
                .setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("500px");

        add(grid);
    }
}
