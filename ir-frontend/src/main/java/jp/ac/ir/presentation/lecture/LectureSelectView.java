package jp.ac.ir.presentation.lecture;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.model.dto.lecture.LectureList;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import jp.ac.ir.service.lecture.LectureService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@PageTitle("授業絞り込み")
@Route(value = "lecture", layout = MainLayout.class)
@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
public class LectureSelectView extends VerticalLayout implements BreadcrumbProvider {

    private final LectureService lectureService;

    private final RadioButtonGroup<String> gradeFilter = new RadioButtonGroup<>("対象学年");
    private final RadioButtonGroup<String> deptFilter = new RadioButtonGroup<>("対象学科");
    private final RadioButtonGroup<String> categoryFilter = new RadioButtonGroup<>("必選別");
    private final RadioButtonGroup<String> semesterFilter = new RadioButtonGroup<>("開講学期");


    private final TextField searchField = new TextField("検索");
    private final Span teacherNote = new Span("最新年度の情報を表示しています");

    private final Grid<LectureList> lectureGrid = new Grid<>(LectureList.class, false);

    public LectureSelectView(LectureService lectureService) {
        this.lectureService = lectureService;

        setWidth("100%");
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("授業絞り込み");

        configureFilters();
        configureLectureGrid();

        teacherNote.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout gridHeader = new HorizontalLayout();
        gridHeader.setWidthFull();
        gridHeader.setJustifyContentMode(JustifyContentMode.START);
        gridHeader.add(teacherNote);

        add(title, createFilterLayout(), gridHeader, lectureGrid);
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        return List.of(
                BreadcrumbItem.of("科目情報", LectureSelectView.class)
        );
    }

    // -----------------------------
    // フィルタ設定
    // -----------------------------
    private void configureFilters() {

        gradeFilter.setItems(lectureService.getDisplayTargetGrades());
        gradeFilter.setValue("全体");

        deptFilter.setItems(lectureService.getDisplayDepartments());
        deptFilter.setValue("全体");

        categoryFilter.setItems(lectureService.getDisplayCategories());
        categoryFilter.setValue("全体");

        semesterFilter.setItems(lectureService.getDisplaySemesters());
        semesterFilter.setValue("全体");

        searchField.setPlaceholder("授業名 または 教員名を検索");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        gradeFilter.addValueChangeListener(e -> updateLectureGrid());
        deptFilter.addValueChangeListener(e -> updateLectureGrid());
        categoryFilter.addValueChangeListener(e -> updateLectureGrid());
        semesterFilter.addValueChangeListener(e -> updateLectureGrid());
        searchField.addValueChangeListener(e -> updateLectureGrid());
    }

    // -----------------------------
    // フィルタUI
    // -----------------------------
    private VerticalLayout createFilterLayout() {
        Button clearButton = new Button("条件をクリア", e -> {
            gradeFilter.setValue("全体");
            deptFilter.setValue("全体");
            categoryFilter.setValue("全体");
            semesterFilter.setValue("全体");
            searchField.clear();
            updateLectureGrid();
        });

        VerticalLayout filterLayout = new VerticalLayout(
                gradeFilter, deptFilter, categoryFilter, semesterFilter, searchField, clearButton
        );
        filterLayout.setWidthFull();
        filterLayout.setAlignItems(Alignment.START);
        return filterLayout;
    }

    // -----------------------------
    // Grid設定
    // -----------------------------
    private void configureLectureGrid() {
        lectureGrid.addColumn(LectureList::lectureName)
                .setHeader("授業名")
                .setAutoWidth(true)
                .setFlexGrow(1);

        lectureGrid.addColumn(LectureList::targetGrade)
                .setHeader("対象学年")
                .setAutoWidth(true)
                .setSortable(true);

        lectureGrid.addColumn(LectureList::targetDepartment)
                .setHeader("対象学科")
                .setAutoWidth(true);

        lectureGrid.addColumn(LectureList::lectureTeacher)
                .setHeader("担当教員")
                .setAutoWidth(true);

        lectureGrid.addColumn(LectureList::category)
                .setHeader("必選別")
                .setAutoWidth(true);

        lectureGrid.addColumn(LectureList::creditsCourse)
                .setHeader("単位数")
                .setAutoWidth(true)
                .setSortable(true);

        lectureGrid.addColumn(LectureList::availableSemester)
                .setHeader("開講学期")
                .setAutoWidth(true)
                .setSortable(true);

        lectureGrid.setWidthFull();
        lectureGrid.setHeight("500px");

        lectureGrid.asSingleSelect().addValueChangeListener(event -> {
            LectureList lecture = event.getValue();
            if (lecture != null && lecture.lectureName() != null && !lecture.lectureName().isBlank()) {
                String encodedLectureName = URLEncoder.encode(
                        lecture.lectureName(),
                        StandardCharsets.UTF_8
                );
                String route = "lecture/" + encodedLectureName;
                if (lecture.courseId() != null && !lecture.courseId().isBlank()) {
                    route += "?courseId=" + URLEncoder.encode(lecture.courseId(), StandardCharsets.UTF_8);
                }
                // 💡 今後詳細画面が作られた際、URL競合しないようにナビゲート先を統一
                UI.getCurrent().navigate(route);
            }
        });

        updateLectureGrid();
    }

    // -----------------------------
    // Grid更新
    // -----------------------------
    private void updateLectureGrid() {
        String searchText = searchField.getValue();

        List<LectureList> filtered = lectureService.getLecturesFiltered(
                gradeFilter.getValue(),
                deptFilter.getValue(),
                categoryFilter.getValue(),
                semesterFilter.getValue(),
                searchText
        );

        lectureGrid.setItems(filtered);
    }
}
