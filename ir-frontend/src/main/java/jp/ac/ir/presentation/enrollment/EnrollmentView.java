package jp.ac.ir.presentation.enrollment;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.model.dto.enrollment.GradeDetailResponse;
import jp.ac.ir.model.dto.enrollment.LectureList;
import jp.ac.ir.model.dto.enrollment.YearlyDistribution;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.grade.UnifiedChart;
import jp.ac.ir.presentation.grade.UnifiesChart;
import jp.ac.ir.presentation.lecture.LectureSelectView;
import jp.ac.ir.presentation.lecture.LectureTopView;
import jp.ac.ir.service.enrollment.EnrollmentService;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.stream.Collectors;

@PageTitle("履修者推移")
@Route(value = "/Enrollment-detail/:lectureName", layout = MainLayout.class)
//@JsModule("./components/unified-chart.tsx")
@JsModule("./components/unifies-chart.tsx")
@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
public class EnrollmentView extends VerticalLayout implements BeforeEnterObserver, BreadcrumbProvider {

    private final EnrollmentService enrollmentService;
    private String lectureName;
    private String routeCourseId;
    private GradeDetailResponse currentDetail;
    private String currentCourseId;
    private String currentLectureDisplayName;
    private final Div chartContainer = new Div();
    private final H2 lectureNameLabel = new H2();
    private final Span teacherLabel = new Span();

    private final Span averageLabel = new Span();
    private final Span varianceLabel = new Span();

    private final RadioButtonGroup<String> comparisonModeSelect = new RadioButtonGroup<>("表示モード");
//    private final Button percentageChartToggleButton = new Button("棒グラフ表示");
//
//    private boolean showPercentageBarChart = false;
    private Map<String, String> selectedCourseLabels = new LinkedHashMap<>();

    public EnrollmentView(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("履修者推移");
        title.getStyle().set("margin", "0");

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setAlignItems(Alignment.BASELINE);
        headerLayout.setSpacing(true);

        lectureNameLabel.getStyle().set("margin", "0");
        teacherLabel.getStyle()
                .set("font-size", "0.95rem")
                .set("color", "var(--lumo-secondary-text-color)");

        headerLayout.add(lectureNameLabel, teacherLabel);

        Button selectSubjectsButton = new Button("科目を比較する");
        selectSubjectsButton.addClickListener(e -> openSubjectSelectionDialog());
        
        comparisonModeSelect.setItems("履修者数", "履修者割合");
        comparisonModeSelect.setValue("履修者数");
        comparisonModeSelect.addValueChangeListener(e -> updateChart());

        HorizontalLayout controls = new HorizontalLayout(selectSubjectsButton, comparisonModeSelect);

        controls.setAlignItems(Alignment.END);
        controls.setSpacing(true);

        VerticalLayout chartWrapper = new VerticalLayout();
        chartWrapper.setWidth("100%");
        chartWrapper.getStyle().set("max-width", "900px").set("margin", "0 auto");
        chartContainer.setWidth("100%");
        chartContainer.setHeight("400px");

        chartWrapper.add(chartContainer);

        HorizontalLayout statsLayout = new HorizontalLayout(averageLabel, varianceLabel);
        statsLayout.setSpacing(true);
        statsLayout.setWidth("100%");
        statsLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        Button backButton = new Button("戻る", e ->
                UI.getCurrent().navigate(createLectureTopRoute())
        );
        backButton.setThemeName("tertiary");

        add(title, headerLayout, controls, chartWrapper, statsLayout, backButton);
    }

    private void openSubjectSelectionDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("比較する科目を選択");
        dialog.setWidth("80vw");
        dialog.setMaxWidth("900px");
        dialog.setHeight("80vh");

        List<LectureList> allLectures = enrollmentService.getLectures();

        // フィルタコンポーネント
        TextField searchField = new TextField();
        searchField.setPlaceholder("授業名や教員名で検索...");
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        ComboBox<String> gradeFilter = new ComboBox<>("対象学年");
        List<String> grades = allLectures.stream()
                .map(LectureList::targetGrade)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        grades.add(0, "全体");
        gradeFilter.setItems(grades);
        gradeFilter.setValue("全体");

        ComboBox<String> deptFilter = new ComboBox<>("対象学科");
        List<String> departments = allLectures.stream()
                .map(LectureList::targetDepartment)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        departments.add(0, "全体");
        deptFilter.setItems(departments);
        deptFilter.setValue("全体");

        HorizontalLayout filterLayout = new HorizontalLayout(gradeFilter, deptFilter);
        filterLayout.setSpacing(true);

        // グリッド
        Grid<LectureList> grid = new Grid<>(LectureList.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        grid.addColumn(LectureList::lectureName)
                .setHeader("授業名")
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1);

        grid.addColumn(l -> {
                    if (l.availableSemester() == null) {
                        return "";
                    }
                    if (l.availableSemester() == 1) {
                        return "前期";
                    }
                    if (l.availableSemester() == 2) {
                        return "後期";
                    }
                    return String.valueOf(l.availableSemester());
                })
                .setHeader("開講期")
                .setAutoWidth(true)
                .setResizable(true);

        grid.addColumn(LectureList::lectureTeacher)
                .setHeader("担当教員")
                .setAutoWidth(true)
                .setResizable(true);

        grid.addColumn(LectureList::targetGrade)
                .setHeader("対象学年")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);

        grid.addColumn(LectureList::targetDepartment)
                .setHeader("対象学科")
                .setAutoWidth(true)
                .setResizable(true);
        
        grid.setItems(allLectures);

        // フィルタリングロジック
        Runnable updateGrid = () -> {
            String searchTerm = searchField.getValue() != null ? searchField.getValue().toLowerCase() : "";
            String selectedGrade = gradeFilter.getValue();
            String selectedDept = deptFilter.getValue();

            List<LectureList> filteredLectures = allLectures.stream()
                    .filter(l -> (l.lectureName() != null && l.lectureName().toLowerCase().contains(searchTerm)) ||
                                 (l.lectureTeacher() != null && l.lectureTeacher().toLowerCase().contains(searchTerm)))
                    .filter(l -> "全体".equals(selectedGrade) || selectedGrade.equals(l.targetGrade()))
                    .filter(l -> "全体".equals(selectedDept) || selectedDept.equals(l.targetDepartment()))
                    .collect(Collectors.toList());
            
            grid.setItems(filteredLectures);
            
            // フィルタリング後も選択状態を維持
            filteredLectures.stream()
                    .filter(l -> l.courseId() != null && selectedCourseLabels.containsKey(l.courseId()))
                    .forEach(grid::select);
        };

        searchField.addValueChangeListener(e -> updateGrid.run());
        gradeFilter.addValueChangeListener(e -> updateGrid.run());
        deptFilter.addValueChangeListener(e -> updateGrid.run());

        // 既に選択されている科目をグリッド上で選択状態にする
        allLectures.stream()
                .filter(l -> l.courseId() != null && selectedCourseLabels.containsKey(l.courseId()))
                .forEach(grid::select);

        VerticalLayout dialogLayout = new VerticalLayout(searchField, filterLayout, grid);
        dialogLayout.setSizeFull();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialog.add(dialogLayout);

        Button saveButton = new Button("決定", e -> {
            selectedCourseLabels = grid.getSelectedItems().stream()
                    .filter(l -> l.courseId() != null)
                    .collect(Collectors.toMap(
                            LectureList::courseId,
                            this::createLectureDisplayName,
                            (v1, v2) -> v1,
                            LinkedHashMap::new
                    ));

            dialog.close();
            updateChart();
        });
        
        Button cancelButton = new Button("キャンセル", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        dialog.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        String parameter = event.getRouteParameters().get("lectureName").orElse("");

        if (parameter.isBlank()) {
            lectureNameLabel.setText("講義が指定されていません");
            return;
        }

        this.lectureName =
                java.net.URLDecoder.decode(parameter, java.nio.charset.StandardCharsets.UTF_8);
        this.routeCourseId = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("courseId", List.of(""))
                .stream()
                .findFirst()
                .orElse("");
        this.routeCourseId = java.net.URLDecoder.decode(
                routeCourseId,
                java.nio.charset.StandardCharsets.UTF_8
        );

        this.currentDetail = getInitialLectureDetail(lectureName);

        if (currentDetail == null) {
            lectureNameLabel.setText("講義データが見つかりません");
            return;
        }

        lectureNameLabel.setText(
                currentLectureDisplayName != null && !currentLectureDisplayName.isBlank()
                        ? currentLectureDisplayName
                        : currentDetail.lectureName() != null ? currentDetail.lectureName() : lectureName
        );

        currentDetail.distributions().stream()
                .max(Comparator.comparing(YearlyDistribution::year))
                .ifPresent(dist -> {
                    teacherLabel.setText(dist.lectureTeacher());
                    averageLabel.setText("平均: " + String.format("%.2f", Optional.ofNullable(dist.average()).orElse(0.0)));
                    varianceLabel.setText("標準偏差: " + formatStandardDeviation(dist.standardDeviation()));
                });

        updateChart();
    }

    private GradeDetailResponse getInitialLectureDetail(String lectureName) {
        currentCourseId = null;
        currentLectureDisplayName = normalizeText(lectureName);

        if (routeCourseId != null && !routeCourseId.isBlank()) {
            currentCourseId = routeCourseId;
            try {
                enrollmentService.getLectures().stream()
                        .filter(l -> routeCourseId.equals(l.courseId()))
                        .findFirst()
                        .ifPresent(l -> currentLectureDisplayName = createLectureDisplayName(l));
            } catch (RestClientException ignored) {
                // Keep the route course_id and fall back to the detail API below.
            }

            GradeDetailResponse detail = getLectureDetailByCourseId(routeCourseId);
            if (detail != null) {
                return detail;
            }
        }

        Optional<LectureList> matchingLecture;

        try {
            matchingLecture = enrollmentService.getLectures().stream()
                    .filter(l -> Objects.equals(normalizeText(l.lectureName()), normalizeText(lectureName)))
                    .filter(l -> l.courseId() != null && !l.courseId().isBlank())
                    .findFirst();
        } catch (RestClientException e) {
            return enrollmentService.getLectureDetail(lectureName);
        }

        if (matchingLecture.isPresent()) {
            LectureList baseLecture = matchingLecture.get();

            currentCourseId = baseLecture.courseId();
            currentLectureDisplayName = createLectureDisplayName(baseLecture);

            GradeDetailResponse detail = getLectureDetailByCourseId(baseLecture.courseId());

            if (detail != null) {
                return detail;
            }
        }

        return enrollmentService.getLectureDetail(lectureName);
    }

    private GradeDetailResponse getLectureDetailByCourseId(String courseId) {
        try {
            return enrollmentService.getLectureDetailByCourseId(courseId);
        } catch (RestClientException e) {
            return null;
        }
    }

    private void updateComparisonChart() {

        boolean percentageMode = "履修者割合".equals(comparisonModeSelect.getValue());

        Map<String, Map<String, Double>> multiSeriesData = new LinkedHashMap<>();

        double maxValue = 0.0;

        // 1. まず、もともと開いている授業を追加
        String baseDisplayName =
                currentLectureDisplayName != null && !currentLectureDisplayName.isBlank()
                        ? currentLectureDisplayName
                        : currentDetail.lectureName() != null && !currentDetail.lectureName().isBlank()
                          ? currentDetail.lectureName()
                          : lectureName;

        maxValue = Math.max(
                maxValue,
                addDetailToMultiSeries(
                        currentDetail,
                        baseDisplayName,
                        percentageMode,
                        multiSeriesData
                )
        );

        // 2. 次に、選択した比較科目を追加
        for (Map.Entry<String, String> selectedCourse : selectedCourseLabels.entrySet()) {

            String courseId = selectedCourse.getKey();
            String displayName = selectedCourse.getValue();

            // もともと開いている授業と同じ course_id なら重複表示しない
            if (currentCourseId != null && currentCourseId.equals(courseId)) {
                continue;
            }

            GradeDetailResponse detail = getLectureDetailByCourseId(courseId);

            if (detail == null || detail.distributions() == null || detail.distributions().isEmpty()) {
                continue;
            }

            if (displayName == null || displayName.isBlank()) {
                displayName = detail.lectureName() != null && !detail.lectureName().isBlank()
                        ? detail.lectureName()
                        : courseId;
            }

            maxValue = Math.max(
                    maxValue,
                    addDetailToMultiSeries(
                            detail,
                            displayName,
                            percentageMode,
                            multiSeriesData
                    )
            );
        }

        if (multiSeriesData.isEmpty()) {
            return;
        }

        Map<String, Map<String, Double>> sortedMultiSeriesData = multiSeriesData.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        UnifiesChart chart = new UnifiesChart();
        chart.setSizeFull();

        chart.setMultiSeriesChartData(sortedMultiSeriesData);

        if (percentageMode) {
            chart.setMaxValue(100);
            chart.setSeriesLabel("履修者割合");
            chart.setYAxisLabel("履修者割合(%)");
        } else {
            chart.setMaxValue(roundUpNice((int) Math.ceil(maxValue)));
            chart.setSeriesLabel("履修者数");
            chart.setYAxisLabel("履修者数");
        }

        chartContainer.add(chart);
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        if (lectureName == null || lectureName.isBlank()) {
            return List.of(
                    BreadcrumbItem.of("成績情報", LectureSelectView.class),
                    BreadcrumbItem.of("履修者推移", EnrollmentView.class)
            );
        }

        return List.of(
                BreadcrumbItem.of("成績情報", LectureSelectView.class),
                BreadcrumbItem.of(
                        lectureName,
                        LectureTopView.class,
                        new RouteParameters("lectureName", lectureName),
                        createCourseIdQueryParameters()
                ),
                BreadcrumbItem.of("履修者推移", EnrollmentView.class, new RouteParameters("lectureName", lectureName))
        );
    }

    private void updateChart() {

        chartContainer.removeAll();

        if (currentDetail == null || currentDetail.distributions() == null) {
            return;
        }

        if (!selectedCourseLabels.isEmpty()) {
            updateComparisonChart();
            return;
        }

        if ("履修者割合".equals(comparisonModeSelect.getValue())) {
            updatePercentageChart();
            return;
        }

        Map<String, Integer> enrollmentData = currentDetail.distributions().stream()
                .sorted(Comparator.comparing(YearlyDistribution::year))
                .collect(Collectors.toMap(
                        dist -> String.valueOf(dist.year()),
                        YearlyDistribution::totalStudents,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        if (enrollmentData.isEmpty()) return;

        int maxEnrollment = enrollmentData.values().stream().max(Integer::compareTo).orElse(0);
        int niceMax = roundUpNice(maxEnrollment);

        UnifiesChart chart = new UnifiesChart();
        chart.setSizeFull();
        chart.setChartData(enrollmentData);
        chart.setMaxValue(niceMax);

        chartContainer.add(chart);
    }

    private void updatePercentageChart() {
        Map<String, Double> enrollmentRateData = currentDetail.distributions().stream()
                .sorted(Comparator.comparing(YearlyDistribution::year))
                .collect(Collectors.toMap(
                        dist -> String.valueOf(dist.year()),
                        dist -> Optional.ofNullable(dist.enrollmentRate()).orElse(0.0),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        if (enrollmentRateData.isEmpty()) return;

        UnifiesChart chart = new UnifiesChart();
        chart.setSizeFull();
        chart.setChartData(enrollmentRateData);
        chart.setMaxValue(100);
        chart.setSeriesLabel("履修者割合");
        chart.setYAxisLabel("履修者割合(%)");

        chartContainer.add(chart);
    }

    private String formatStandardDeviation(Double standardDeviation) {
        double safeValue = Optional.ofNullable(standardDeviation).orElse(0.0);
        return String.format("%.2f", safeValue);
    }
    private String createLectureDisplayName(LectureList lecture) {
        String lectureName = normalizeText(lecture.lectureName());
        String semesterLabel = semesterLabel(lecture.availableSemester());

        if (!semesterLabel.isBlank()) {
            return lectureName + "（" + semesterLabel + "）";
        }

        return lectureName;
    }

    private String semesterLabel(Integer availableSemester) {
        if (availableSemester == null) {
            return "";
        }
        if (availableSemester == 1) {
            return "前期";
        }
        if (availableSemester == 2) {
            return "後期";
        }
        return String.valueOf(availableSemester);
    }
    private String createLectureKey(LectureList lecture) {
        return String.join("::",
                normalizeText(lecture.lectureName()),
                normalizeText(lecture.lectureTeacher()),
                normalizeText(lecture.targetGrade()),
                normalizeText(lecture.targetDepartment())
        );
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
    private int roundUpNice(int value) {

        if (value <= 10) return 10;
        if (value <= 50) return ((value + 9) / 10) * 10;
        if (value <= 100) return ((value + 9) / 10) * 10;
        if (value <= 500) return ((value + 49) / 50) * 50;

        return ((value + 99) / 100) * 100;
    }

    private double addDetailToMultiSeries(
            GradeDetailResponse detail,
            String displayName,
            boolean percentageMode,
            Map<String, Map<String, Double>> multiSeriesData
    ) {
        if (detail == null || detail.distributions() == null || detail.distributions().isEmpty()) {
            return 0.0;
        }

        double maxValue = 0.0;

        String safeDisplayName =
                displayName != null && !displayName.isBlank()
                        ? displayName
                        : detail.lectureName();

        for (YearlyDistribution dist : detail.distributions().stream()
                .sorted(Comparator.comparing(YearlyDistribution::year))
                .toList()) {

            String year = String.valueOf(dist.year());

            double value;

            if (percentageMode) {
                value = Optional.ofNullable(dist.enrollmentRate()).orElse(0.0);
            } else {
                value = dist.totalStudents();
            }

            maxValue = Math.max(maxValue, value);

            multiSeriesData
                    .computeIfAbsent(year, k -> new LinkedHashMap<>())
                    .put(safeDisplayName, value);
        }

        return maxValue;
    }

    private String createLectureTopRoute() {
        String route = "lecture/" + java.net.URLEncoder.encode(
                lectureName,
                java.nio.charset.StandardCharsets.UTF_8
        );
        if (currentCourseId != null && !currentCourseId.isBlank()) {
            route += "?courseId=" + java.net.URLEncoder.encode(
                    currentCourseId,
                    java.nio.charset.StandardCharsets.UTF_8
            );
        }
        return route;
    }

    private QueryParameters createCourseIdQueryParameters() {
        if (currentCourseId == null || currentCourseId.isBlank()) {
            return null;
        }
        return QueryParameters.simple(Map.of("courseId", currentCourseId));
    }
}
