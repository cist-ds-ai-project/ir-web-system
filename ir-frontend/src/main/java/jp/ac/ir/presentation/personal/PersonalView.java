package jp.ac.ir.presentation.personal;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.model.dto.personal.PersonalGpa;
import jp.ac.ir.model.dto.personal.PersonalGrade;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.grade.UnifiedChart;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import jp.ac.ir.service.management.LoginUser;
import jp.ac.ir.service.management.SecurityService;
import jp.ac.ir.service.personal.PersonalService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RolesAllowed("student")
@Route(value = "personal", layout = MainLayout.class)
@PageTitle("個人成績")
public class PersonalView extends VerticalLayout implements BreadcrumbProvider {

    private final PersonalService personalService;

    // ログインしている学生
    private final LoginUser loginUser;

    private final String personId;

    private final UnifiedChart gradeChart = new UnifiedChart();

    private final Grid<PersonalGrade> grid = new Grid<>(PersonalGrade.class, false);

    private final Grid<GpaRow> gpaGrid = new Grid<>(GpaRow.class, false);private final RadioButtonGroup<String> yearGroup = new RadioButtonGroup<>();

    private final RadioButtonGroup<String> semesterGroup = new RadioButtonGroup<>();

    private final RadioButtonGroup<String> categoryGroup = new RadioButtonGroup<>();

    private final TextField lectureSearch = new TextField("授業検索");

    private final HorizontalLayout filterChartArea = new HorizontalLayout();

    private record GpaRow(Integer year, String springGpa, String autumnGpa) {}

    private Grid.Column<GpaRow> springColumn;

    private Grid.Column<GpaRow> autumnColumn;
    public PersonalView(PersonalService personalService, SecurityService securityService) {


        this.personalService = personalService;
        this.loginUser = securityService.getLoginUser();
        this.personId = loginUser.getPersonId();

        setWidthFull();
        setHeight(null);
        getStyle().set("overflow", "visible");

        createHeader();

        createFilterArea();

        createChart();

        createGrid();

        createGpaGrid();
        // 初期表示
        refreshGrid();

        refreshGpa();
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        return List.of(BreadcrumbItem.of("個人成績", PersonalView.class));
    }


    // ヘッダー(仮で固定の学生を使用)
    private void createHeader() {

        H1 title = new H1("個人成績");

        Span student = new Span(personId + " " + loginUser.getUsername());

        HorizontalLayout header = new HorizontalLayout();

        header.add(title, student);

        header.setAlignItems(Alignment.CENTER);

        add(header);
    }


    // フィルタ
    private void createFilterArea() {

        // 年度
        yearGroup.setLabel("年度");
        List<String> years = personalService.getDisplayYears(personId);
        yearGroup.setItems(years);
        if (!years.isEmpty()) {
            yearGroup.setValue("全年度");
        }

        // 学期
        semesterGroup.setLabel("学期");
        List<String> semesters = personalService.getDisplaySemesters();
        semesterGroup.setItems(semesters);
        if (!semesters.isEmpty()) {
            semesterGroup.setValue("全体");
        }

        // 必選区分
        categoryGroup.setLabel("必選区分");
        List<String> categories = personalService.getDisplayCategories();
        categoryGroup.setItems(categories);
        if (!categories.isEmpty()) {
            categoryGroup.setValue("全体");
        }
        // フィルタ変更時にGrid更新
        yearGroup.addValueChangeListener(e -> refreshGrid());
        semesterGroup.addValueChangeListener(e -> refreshGrid());
        categoryGroup.addValueChangeListener(e -> refreshGrid());
        lectureSearch.addValueChangeListener(e -> refreshGrid());


        VerticalLayout filter = new VerticalLayout(
                        yearGroup,
                        semesterGroup,
                        categoryGroup,
                        lectureSearch
                );

        filter.setWidth("30%");
        filter.setPadding(false);
        filter.setSpacing(false);


        HorizontalLayout topArea = new HorizontalLayout();

        topArea.setWidthFull();
        topArea.setHeight("350px");
        topArea.setAlignItems(Alignment.START);

        topArea.add(filter);
        topArea.add(filterChartArea);

        topArea.setFlexGrow(1, filterChartArea);

        add(topArea);
    }

    private void createChart() {

        gradeChart.setChartType("bar");
        gradeChart.setUnit("個");
        gradeChart.setLabel("個数");
        gradeChart.setHeight("300px");
        gradeChart.setWidthFull();

        filterChartArea.setWidth("70%");
        filterChartArea.setHeight("350px");
        filterChartArea.setPadding(false);
        filterChartArea.setSpacing(false);

        filterChartArea.add(gradeChart);
    }


    private void refreshChart() {

        Map<String, Integer> counts =
                personalService.getGradeCountsFiltered(
                        yearGroup.getValue(),
                        semesterGroup.getValue(),
                        categoryGroup.getValue(),
                        lectureSearch.getValue(),
                        personId
                );

        List<String> gradeOrder = personalService.getDisplayGrades();

        gradeChart.setChartData(counts);
        gradeChart.setGradeOrder(gradeOrder);
    }


    // Gridを更新
    private void refreshGrid() {

        List<PersonalGrade> grades =
                personalService.getGradesFiltered(
                        yearGroup.getValue(),
                        semesterGroup.getValue(),
                        categoryGroup.getValue(),
                        lectureSearch.getValue(),
                        personId
                );

        grid.setItems(grades);
        refreshChart();
    }


    // Grid
    private void createGrid() {

        grid.addComponentColumn(grade -> {
                    Button button = new Button(grade.lectureName());
                    button.addClickListener(e -> {
                        Map<String, String> params = new LinkedHashMap<>();

                        params.put("lectureName", grade.lectureName());
                        params.put("lectureTeacher", grade.lectureTeacher());
                        params.put("year", String.valueOf(grade.year()));
                        params.put("category", grade.category());
                        params.put("semester", grade.semester());
                        params.put("creditsCourse", String.valueOf(grade.creditsCourse()));
                        params.put("grading", grade.grading());
                        params.put("gp", String.valueOf(grade.gp()));
                        params.put("attendance", String.valueOf(grade.attendance()));
                        params.put("lateness", String.valueOf(grade.lateness()));
                        params.put("absence", String.valueOf(grade.absence()));
                        params.put("courseId", String.valueOf(grade.courseId()));


                        UI.getCurrent().navigate(PersonalDetailView.class, QueryParameters.simple(params));
                    });
                    button.getStyle()
                            .set("background", "none")
                            .set("border", "none")
                            .set("padding", "0")
                            .set("color", "inherit")
                            .set("cursor", "pointer");
                    return button;
                })
                .setHeader("科目名")
                .setWidth("380px")
                .setFlexGrow(0);

        grid.addColumn(PersonalGrade::lectureTeacher)
                .setHeader("担当教員")
                .setWidth("300px")
                .setFlexGrow(0);

        grid.addColumn(PersonalGrade::year)
                .setHeader("年度")
                .setWidth("100px")
                .setFlexGrow(0);

        grid.addColumn(PersonalGrade::category)
                .setHeader("必選区分")
                .setWidth("130px")
                .setFlexGrow(0);

        grid.addColumn(PersonalGrade::semester)
                .setHeader("学期")
                .setWidth("110px")
                .setFlexGrow(0);

        grid.addColumn(PersonalGrade::creditsCourse)
                .setHeader("単位")
                .setWidth("80px")
                .setFlexGrow(0);

        grid.addColumn(PersonalGrade::grading)
                .setHeader("評価")
                .setWidth("100px")
                .setFlexGrow(0);

        grid.addColumn(PersonalGrade::gp)
                .setHeader("GP")
                .setWidth("100px")
                .setFlexGrow(0);

        grid.addColumn(PersonalGrade::attendance)
                .setHeader("出席")
                .setWidth("100px")
                .setFlexGrow(0);

        grid.addColumn(PersonalGrade::lateness)
                .setHeader("遅刻")
                .setWidth("100px")
                .setFlexGrow(0);

        grid.addColumn(PersonalGrade::absence)
                .setHeader("欠席")
                .setWidth("100px")
                .setFlexGrow(0);


        grid.setSizeFull();
        grid.setHeight("500px");

        add(grid);
    }

    private void refreshGpa() {

        List<PersonalGpa> gpas =
                personalService.getPersonalGpa(personId);

        Map<Integer, GpaRow> rows =
                new LinkedHashMap<>();

        String springSemester = "春学期";
        String autumnSemester = "秋学期";

        for (PersonalGpa gpa : gpas) {

            Integer year = gpa.year();

            if (year == null) {
                continue;
            }

            GpaRow current = rows.get(year);

            String spring = current != null
                    ? current.springGpa()
                    : "-";

            String autumn = current != null
                    ? current.autumnGpa()
                    : "-";

            if ("春学期".equals(gpa.semester()) || "第1学期".equals(gpa.semester())) {

                spring = String.format("%.2f", gpa.gpa());
                springSemester = gpa.semester();

            } else if ("秋学期".equals(gpa.semester()) || "第2学期".equals(gpa.semester())) {

                autumn = String.format("%.2f", gpa.gpa());
                autumnSemester = gpa.semester();
            }

            rows.put(year, new GpaRow(year, spring, autumn));
        }

        gpaGrid.setItems(rows.values());

        // GPAの行数に合わせて高さを調整
        int rowCount = rows.size();
        int height = 45 + rowCount * 45;

        gpaGrid.setHeight(height + "px");

        springColumn.setHeader(springSemester);
        autumnColumn.setHeader(autumnSemester);

    }

    private void createGpaGrid() {

        H2 title = new H2("GPA");

        gpaGrid.addColumn(row -> row.year())
                .setHeader("年度")
                .setAutoWidth(true);

        springColumn = gpaGrid.addColumn(GpaRow::springGpa)
                .setHeader("春学期")
                .setAutoWidth(true);

        autumnColumn = gpaGrid.addColumn(GpaRow::autumnGpa)
                .setHeader("秋学期")
                .setAutoWidth(true);

        gpaGrid.setWidth("500px");

        gpaGrid.setWidth("300px");
        gpaGrid.setHeight(null);

        VerticalLayout gpaArea =
                new VerticalLayout(title, gpaGrid);

        gpaArea.setWidth("500px");
        gpaArea.setHeight("250px");
        gpaArea.setPadding(false);
        gpaArea.setSpacing(false);

        add(gpaArea);
    }
}