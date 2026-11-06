package jp.ac.ir.presentation.grade;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.model.dto.grade.GradeDetailResponse;
import jp.ac.ir.model.dto.grade.YearlyDistribution;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.lecture.LectureSelectView;
import jp.ac.ir.presentation.lecture.LectureTopView;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import jp.ac.ir.presentation.personal.PersonalDetailView;
import jp.ac.ir.presentation.questionnaire.QuestionnaireView;
import jp.ac.ir.service.grade.GradeService;

import java.util.*;

@PageTitle("成績分布")
@Route(value = "/grade-detail/:lectureName", layout = MainLayout.class)
@JsModule("./components/unified-chart.tsx")
@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
public class GradeView extends VerticalLayout implements BeforeEnterObserver, BreadcrumbProvider {

    private final GradeService gradeService;

    private final HorizontalLayout comparisonLayout = new HorizontalLayout();
    private String lectureName;
    private String from;
    private String courseId;
    private Map<String, String> personalDetailParams = new LinkedHashMap<>();
    private GradeDetailResponse currentDetail;
    private final Div chartsWrapper = new Div();
    private final CheckboxGroup<Integer> yearSelect = new CheckboxGroup<>("年度選択");
    private final RadioButtonGroup<Integer> comparisonYearSelect = new RadioButtonGroup<>("比較年度");
    private enum DisplayMode{ COUNT, PERCENTAGE, COMPARISON}
    private DisplayMode displayMode = DisplayMode.COUNT;

    private final Button countButton = new Button("人数表示");
    private final Button percentageButton = new Button("%表示");
    private final Button comparisonButton = new Button("前年度比較");

    private final H2 lectureNameLabel = new H2();
    private final Span teacherLabel = new Span();
    private List<String> gradeLabels = new ArrayList<>();

    public GradeView(GradeService gradeService) {
        this.gradeService = gradeService;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setAlignItems(Alignment.BASELINE);
        headerLayout.setSpacing(true);

        lectureNameLabel.getStyle().set("margin", "0");
        teacherLabel.getStyle()
                .set("font-size", "0.95rem")
                .set("color", "var(--lumo-secondary-text-color)");

        headerLayout.add(lectureNameLabel, teacherLabel);

        Button backButton = new Button("戻る", e -> {

            if ("questionnaire".equals(from)) {

                UI.getCurrent().navigate(
                        QuestionnaireView.class,
                        new RouteParameters(
                                "lectureName",
                                lectureName
                        )
                );

            } else if ("personalDetail".equals(from)) {

                UI.getCurrent().navigate(
                        PersonalDetailView.class,
                        QueryParameters.simple(personalDetailParams)
                );

            } else {

                UI.getCurrent().navigate(
                        LectureTopView.class,
                        new RouteParameters(
                                "lectureName",
                                lectureName
                        )
                );
            }
        });
        backButton.setThemeName("tertiary");

        H2 title = new H2("成績分布");
        title.getStyle().set("margin", "0");

        yearSelect.setLabel("年度選択");
        yearSelect.addValueChangeListener(e -> updateChartAndSummary());

        countButton.addClickListener(e -> {
            displayMode = DisplayMode.COUNT;
            comparisonLayout.setVisible(false);
            comparisonYearSelect.setVisible(false);
            yearSelect.setVisible(true);
            updateChartAndSummary();
        });
        percentageButton.addClickListener(e -> {
            displayMode = DisplayMode.PERCENTAGE;
            comparisonLayout.setVisible(false);
            comparisonYearSelect.setVisible(false);
            yearSelect.setVisible(true);
            updateChartAndSummary();
        });
        comparisonButton.addClickListener(e -> {
            displayMode = DisplayMode.COMPARISON;
            createComparisonYearSelect();
            comparisonLayout.setVisible(true);
            yearSelect.setVisible(false);
            updateChartAndSummary();
        });
        HorizontalLayout buttons = new HorizontalLayout(countButton, percentageButton, comparisonButton);

        HorizontalLayout controls = new HorizontalLayout(buttons,comparisonLayout);
        controls.setAlignItems(Alignment.END);
        controls.setSpacing(true);

        chartsWrapper.setWidthFull();
        chartsWrapper.removeAll();

        if (displayMode == DisplayMode.COUNT) {
            yearSelect.setVisible(true);
            comparisonYearSelect.setVisible(false);
            chartsWrapper.getStyle()
                    .set("display", "flex")
                    .set("max-width", "1200px")
                    .set("margin", "0 auto")
                    .set("flex-direction", "column")
                    .remove("grid-template-columns");
        }
        else if (displayMode == DisplayMode.PERCENTAGE) {
            yearSelect.setVisible(true);
            comparisonYearSelect.setVisible(false);
            chartsWrapper.getStyle()
                    .set("display", "grid")
                    .set("grid-template-columns", "1fr 1fr")
                    .set("gap", "16px")
                    .remove("flex-direction");
        }
        comparisonYearSelect.setLabel("比較年度");

        comparisonYearSelect.addValueChangeListener(e -> {
            if(displayMode == DisplayMode.COMPARISON){
                yearSelect.setVisible(false);
                comparisonYearSelect.setVisible(true);
                updateChartAndSummary();
            }
        });
        comparisonLayout.add(comparisonYearSelect);
        comparisonLayout.setVisible(false);



        add(title, headerLayout, yearSelect,comparisonYearSelect, controls, chartsWrapper, backButton);
        comparisonYearSelect.setVisible(false);
        yearSelect.setVisible(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        // ルートパラメータから講義名を取得
        String parameter = event.getRouteParameters()
                .get("lectureName")
                .orElse("");

        // クエリパラメータをまとめて取得
        Map<String, List<String>> queryParams =
                event.getLocation()
                        .getQueryParameters()
                        .getParameters();

        // どこから遷移してきたか
        this.from = queryParams
                .getOrDefault("from", List.of(""))
                .stream()
                .findFirst()
                .orElse("");

        // 授業ID
        this.courseId = queryParams
                .getOrDefault("courseId", List.of(""))
                .stream()
                .findFirst()
                .orElse("");

        /*
         * PersonalDetailView から来た場合、
         * 戻るときにPersonalDetailViewの状態を復元できるように
         * 必要なクエリパラメータを保存しておく
         */
        if ("personalDetail".equals(from)) {

            personalDetailParams = new LinkedHashMap<>();

            // lectureNameはURLのルートパラメータなので、
            // queryParamsではなくparameterから取得する
            String decodedLectureName = java.net.URLDecoder.decode(
                    parameter,
                    java.nio.charset.StandardCharsets.UTF_8
            );

            personalDetailParams.put(
                    "lectureName",
                    decodedLectureName
            );

            List<String> keys = List.of(
                    "courseId",
                    "year",
                    "semester",
                    "lectureTeacher",
                    "category",
                    "creditsCourse",
                    "grading",
                    "gp",
                    "attendance",
                    "lateness",
                    "absence"
            );

            for (String key : keys) {

                String value = queryParams
                        .getOrDefault(key, List.of(""))
                        .stream()
                        .findFirst()
                        .orElse("");

                personalDetailParams.put(key, value);
            }
        }

        // 講義名が指定されていない場合
        if (parameter.isBlank()) {
            lectureNameLabel.setText("講義が指定されていません");
            teacherLabel.setText("");
            return;
        }

        // URLエンコードされた講義名をデコード
        this.lectureName = java.net.URLDecoder.decode(
                parameter,
                java.nio.charset.StandardCharsets.UTF_8
        );

        // 講義詳細を取得
        this.currentDetail =
                gradeService.getLectureDetail(lectureName);

        // 講義データが存在しない場合
        if (currentDetail == null) {
            lectureNameLabel.setText("講義データが見つかりません");
            teacherLabel.setText("");
            return;
        }

        // 講義名を表示
        lectureNameLabel.setText(
                currentDetail.lectureName() != null
                        ? currentDetail.lectureName()
                        : lectureName
        );

        // 年度一覧を取得
        List<Integer> years =
                currentDetail.distributions() == null
                        ? List.of()
                        : currentDetail.distributions().stream()
                        .map(d -> d.year())
                        .distinct()
                        .sorted(Comparator.reverseOrder())
                        .toList();

        yearSelect.setItems(years);

        // 最新年度を初期選択
        if (!years.isEmpty()) {
            yearSelect.setValue(Set.of(years.get(0)));
        }

        // 比較年度を取得
        List<Integer> comparisonYears =
                getComparisonYears();

        comparisonYearSelect.setItems(comparisonYears);

        // 比較年度の初期値
        if (!comparisonYears.isEmpty()) {
            comparisonYearSelect.setValue(
                    comparisonYears.get(0)
            );
        }

        // 初期状態では比較表示を非表示
        comparisonYearSelect.setVisible(false);

        // グラフと集計を更新
        updateChartAndSummary();
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        if (lectureName == null || lectureName.isBlank()) {
            return List.of(
                    BreadcrumbItem.of("成績情報", LectureSelectView.class),
                    BreadcrumbItem.of("成績分布", GradeView.class)
            );
        }

        return List.of(
                BreadcrumbItem.of("成績情報", LectureSelectView.class),
                BreadcrumbItem.of(
                        lectureName,
                        LectureTopView.class,
                        new RouteParameters("lectureName", lectureName)
                ),
                BreadcrumbItem.of(
                        "成績分布",
                        GradeView.class,
                        new RouteParameters("lectureName", lectureName)
                )
        );
    }

    private void updateChartAndSummary() {
        chartsWrapper.removeAll();
        chartsWrapper.getStyle().clear();
        chartsWrapper.setWidthFull();
        chartsWrapper.setHeight(null);

        if (displayMode == DisplayMode.PERCENTAGE) {
            chartsWrapper.getStyle()
                    .set("width", "100%")
                    .set("max-width", "none")
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("gap", "16px");

        }
        else if (displayMode == DisplayMode.COMPARISON) {

            chartsWrapper.getStyle()
                    .set("display", "block")
                    .set("width", "100%");

        }
        else {
            chartsWrapper.getStyle()
                    .set("display", "grid")
                    .set("grid-template-columns", "1fr 1fr")
                    .set("gap", "16px");
        }
        if (lectureName == null || lectureName.isBlank()) return;
        if (yearSelect.getValue() == null || yearSelect.getValue().isEmpty()) return;
        if (currentDetail == null || currentDetail.distributions() == null || currentDetail.distributions().isEmpty()) return;

        if (displayMode == DisplayMode.COMPARISON) {

            updateComparisonView();

            return;
        }

        List<YearlyDistribution> selectedDistributions = currentDetail.distributions().stream()
                .filter(d -> yearSelect.getValue().contains(d.year()))
                .sorted(Comparator.comparingInt((YearlyDistribution d) -> d.year()).reversed())
                .toList();
        int rawMax = getGlobalMax(selectedDistributions);
        int niceMax = roundUpNice(rawMax);

        if (selectedDistributions.isEmpty()) return;

        this.gradeLabels = extractGradeLabels(selectedDistributions);

        for (YearlyDistribution dist : selectedDistributions) {
            chartsWrapper.add(createYearSection(dist, niceMax));
        }
    }

    private void updateComparisonView() {

        // 選択年度取得
        Integer selectedYear = comparisonYearSelect.getValue();
        if (selectedYear == null) {
            return;
        }
        // 選択年度
        YearlyDistribution current =
                currentDetail.distributions()
                        .stream()
                        .filter(d -> d.year() == selectedYear)
                        .findFirst()
                        .orElse(null);
        // 前年度
        YearlyDistribution previous =
                currentDetail.distributions()
                        .stream()
                        .filter(d -> d.year() == selectedYear - 1)
                        .findFirst()
                        .orElse(null);


        H3 title = new H3(selectedYear + "年度 前年度比較");
        title.getStyle().set("margin", "0");
        Span teacher = new Span(current.lectureTeacher() != null ? "担当教員：" + current.lectureTeacher() : "");
        teacher.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-left", "24px");

        HorizontalLayout titleRow = new HorizontalLayout(title, teacher);
        titleRow.setAlignItems(Alignment.BASELINE);
        titleRow.setSpacing(false);

        // 前年度データがない場合
        if (current == null || previous == null) {
            chartsWrapper.add(new Span("前年度データが存在しません"));
            return;
        }
        // 評価項目取得
        List<String> labels = extractGradeLabels(List.of(current, previous));
        this.gradeLabels = labels;
        // 最大値
        int max = Math.max(getMax(current), getMax(previous));
        int niceMax = roundUpNice(max);

        //比較用グラフ
        UnifiedChart chart = new UnifiedChart();
        chart.setWidthFull();
        chart.setHeight("260px");
        chart.setGradeOrder(labels);
        chart.setMaxValue(niceMax);
        chart.setChartType("bar");

        // 2年度分を渡す
        Map<String, Map<String,Integer>> comparisonData = new LinkedHashMap<>();

        comparisonData.put(previous.year() + "年度", previous.counts());
        comparisonData.put(current.year() + "年度", current.counts());

        chart.setComparisonData(comparisonData);
        //比較用grid
        Grid<Map<String,Object>> grid = createComparisonGrid(current, previous, labels);
        VerticalLayout layout = new VerticalLayout();
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
        grid.setHeight(null);
        grid.getStyle()
                .set("width","100%");
        layout.setWidthFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setHeight(null);
        layout.getStyle()
                .set("width","100%")
                .set("max-width","none")
                .set("border",
                        "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius",
                        "12px")
                .set("padding",
                        "12px")
                .set("background",
                        "var(--lumo-base-color)");
        layout.add(titleRow, chart, grid);
        chartsWrapper.add(layout);
    }

    private Grid<Map<String,Object>> createComparisonGrid(YearlyDistribution current, YearlyDistribution previous, List<String> labels){
        Grid<Map<String,Object>> grid = new Grid<>();
        grid.addColumn(row -> row.get("type"))
                .setHeader("")
                .setFlexGrow(1);

        grid.addColumn(row -> row.get("students"))
                .setHeader("受講人数")
                .setFlexGrow(1);

        for(String label : labels){
            grid.addComponentColumn(row -> {
                        String value = row.get(label).toString();
                        Span span = new Span(value);
                        // +の場合 赤
                        if(value.startsWith("+")){
                            span.getStyle().set("color", "red");
                        }
                        // -の場合 青
                        else if(value.startsWith("-")){
                            span.getStyle().set("color", "blue");
                        }
                        return span;
                    })
                    .setHeader(label)
                    .setAutoWidth(true);

        }

        List<Map<String,Object>> rows = new ArrayList<>();

        // 選択年度
        rows.add(createComparisonRow(current, labels, current.year()+"年度"));

        // 前年度
        rows.add(createComparisonRow(previous, labels, previous.year()+"年度(前年度)"));

        // 増減数
        rows.add(createDiffRow(current, previous, labels));

        // 増減比
        rows.add(createRateRow(current, previous, labels));
        grid.setItems(rows);
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
        grid.setHeight(null);
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        return grid;
    }

    private Map<String,Object> createComparisonRow(YearlyDistribution dist, List<String> labels, String title){
        Map<String,Object> row = new LinkedHashMap<>();
        row.put("type", title);
        // 受講人数
        int total = dist.counts()
                        .values()
                        .stream()
                        .mapToInt(Integer::intValue)
                        .sum();

        row.put("students", total + "人");

        // 秀・優・良・可・不可
        for(String label : labels){
            int count = dist.counts().getOrDefault(label,0);
            row.put(label, count + "人");
        }
        return row;
    }

    //増減数を生成する
    private Map<String,Object> createDiffRow(YearlyDistribution current, YearlyDistribution previous, List<String> labels){
        Map<String,Object> row = new LinkedHashMap<>();
        row.put("type", "増減数");
        row.put("students", "");
        for(String label : labels){
            int diff = current.counts().getOrDefault(label,0) - previous.counts().getOrDefault(label,0);
            String value;
            if(diff > 0){
                value = "+" + diff + "人";
            }
            else if(diff < 0){
                value = diff + "人";
            }
            else{
                value = "0人";
            }
            row.put(label,value);
        }

        return row;
    }


    //増減比を生成
    private Map<String,Object> createRateRow(YearlyDistribution current, YearlyDistribution previous, List<String> labels){
        Map<String,Object> row = new LinkedHashMap<>();
        row.put("type","増減比");
        row.put("students","");
        for(String label : labels){
            int now = current.counts().getOrDefault(label,0);
            int before = previous.counts().getOrDefault(label,0);
            String value;
            if(before == 0){
                value="-";
            }
            else{
                double rate = ((double)(now-before) / before) *100;
                value = String.format("%+.1f%%", rate);
            }
            row.put(label,value);
        }
        return row;
    }



    private Component createYearSection(YearlyDistribution dist, int niceMax) {
        H3 yearTitle = new H3(dist.year() + "年度");
        yearTitle.getStyle().set("margin", "0");

        Span teacher = new Span(dist.lectureTeacher() != null ? "担当教員：" + dist.lectureTeacher() : "");
        teacher.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-left", "24px");

        HorizontalLayout titleRow = new HorizontalLayout(yearTitle, teacher);
        titleRow.setAlignItems(Alignment.BASELINE);
        titleRow.setSpacing(false);

        UnifiedChart chart = new UnifiedChart();
        chart.setWidthFull();
        chart.setGradeOrder(gradeLabels);
        chart.setMaxValue(niceMax);

        Grid<Map<String, Object>> grid;
        if(displayMode == DisplayMode.PERCENTAGE){
            chart.setHeight("150px");
            chart.setChartData(toPercentageMap(dist, gradeLabels));
            chart.setCountData(dist.counts());
            chart.setChartType("band");
            grid = createPercentageSummaryGrid(gradeLabels);
            grid.setItems(List.of(toPercentageSummaryMap(dist, gradeLabels)));
        }
        //人数表示
        else{
            chart.setHeight("260px");
            chart.setChartData(dist.counts());
            chart.setChartType("bar");
            grid = createCountSummaryGrid(gradeLabels);
            grid.setItems(List.of(toCountSummaryMap(dist, gradeLabels)));
        }
        grid.setWidthFull();

        VerticalLayout column = new VerticalLayout();
        grid.setWidth("100%");
        grid.getStyle().set("align-self", "stretch");
        if(displayMode == DisplayMode.PERCENTAGE){
            column.setWidth("100%");
        }
        column.setPadding(false);
        column.setSpacing(false);
        column.setAlignItems(Alignment.STRETCH);
        if(displayMode == DisplayMode.PERCENTAGE){
            column.getStyle()
                    .set("max-width","none");
            chartsWrapper.setWidthFull();
        }
        else{
            column.getStyle()
                    .set("max-width","600px");
        }
        column.getStyle()
                .set("border",
                        "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius",
                        "12px")
                .set("padding",
                        "12px")
                .set("background",
                        "var(--lumo-base-color)");

        column.add(titleRow, chart, grid);
        column.setHorizontalComponentAlignment(Alignment.STRETCH, grid);
        return column;
    }
    private void createComparisonYearSelect(){

        List<Integer> years = currentDetail.distributions()
                .stream()
                .map(YearlyDistribution::year)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
        comparisonYearSelect.setItems(years);
        if(!years.isEmpty()){
            comparisonYearSelect.setValue(years.get(0));
        }
        if(!getChildren()
                .anyMatch(c -> c.equals(comparisonYearSelect))){
            addComponentAtIndex(indexOf(yearSelect), comparisonYearSelect);
            yearSelect.setVisible(false);
        }
    }


    private List<String> extractGradeLabels(List<YearlyDistribution> distributions) {
        if (distributions == null || distributions.isEmpty()) {
            return List.of();
        }

        for (YearlyDistribution dist : distributions) {
            if (dist.counts() != null && !dist.counts().isEmpty()) {
                return new ArrayList<>(dist.counts().keySet());
            }
        }

        return List.of();
    }

    private Grid<Map<String, Object>> createCountSummaryGrid(List<String> gradeLabels) {
        Grid<Map<String, Object>> grid = new Grid<>();

        grid.addColumn(row -> row.get("totalStudents"))
                .setHeader("受講人数")
                .setAutoWidth(true)
                .setFlexGrow(0);

        for (String label : gradeLabels) {
            grid.addColumn(row -> row.get(label))
                    .setHeader(label)
                    .setAutoWidth(true)
                    .setFlexGrow(0);
        }

        grid.addColumn(row -> row.get("average"))
                .setHeader("平均")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(row -> row.get("variance"))
                .setHeader("分散")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid.getStyle().set("font-size", "0.85rem");

        return grid;
    }

    private Grid<Map<String, Object>> createPercentageSummaryGrid(List<String> gradeLabels) {
        Grid<Map<String, Object>> grid = new Grid<>();

        grid.addColumn(row -> row.get("totalStudents"))
                .setHeader("受講人数")
                .setFlexGrow(1);

        for (String label : gradeLabels) {
            grid.addColumn(row -> row.get(label))
                    .setHeader(label)
                    .setFlexGrow(1);
        }

        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid.getStyle().set("font-size", "0.85rem");

        return grid;
    }

    private Map<String, Object> toCountSummaryMap(YearlyDistribution dist, List<String> gradeLabels) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("totalStudents", dist.totalStudents() + "人");

        for (String label : gradeLabels) {
            int count = dist.counts() != null ? dist.counts().getOrDefault(label, 0) : 0;
            row.put(label, count + "人");
        }
        row.put("average", dist.average());
        row.put("variance", dist.variance());

        return row;
    }

    private Map<String, Double> toPercentageMap(YearlyDistribution dist, List<String> gradeLabels) {
        Map<String, Double> result = new LinkedHashMap<>();
        Map<String, String> percentages = gradeService.getPercentages(dist);

        for (String label : gradeLabels) {
            String raw = percentages.getOrDefault(label, "0.0%");
            String numeric = raw.replace("%", "").trim();

            try {
                result.put(label, Double.parseDouble(numeric));
            } catch (NumberFormatException e) {
                result.put(label, 0.0);
            }
        }

        return result;
    }

    private Map<String, Object> toPercentageSummaryMap(YearlyDistribution dist, List<String> gradeLabels) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("totalStudents", dist.totalStudents() + "人");

        Map<String, String> percentages = gradeService.getPercentages(dist);
        for (String label : gradeLabels) {
            String percent = percentages.getOrDefault(label, "0.0%");
            row.put(label, percent);
        }

        return row;
    }
    private List<Integer> getComparisonYears(){

        if(currentDetail == null || currentDetail.distributions() == null){
            return List.of();
        }


        Set<Integer> years = currentDetail.distributions()
                        .stream()
                        .map(YearlyDistribution::year)
                        .collect(TreeSet::new, Set::add, Set::addAll);
        return years.stream()
                .filter(year -> years.contains(year - 1))
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    private int getGlobalMax(List<YearlyDistribution> list) {
        return list.stream()
                .flatMap(d -> d.counts().values().stream())
                .max(Integer::compareTo)
                .orElse(0);
    }

    private int roundUpNice(int value) {
        if (value <= 10) return 10;
        if (value <= 50) return ((value + 9) / 10) * 10;
        if (value <= 100) return ((value + 9) / 10) * 10;
        if (value <= 500) return ((value + 49) / 50) * 50;
        return ((value + 99) / 100) * 100;
    }
    private int getMax(YearlyDistribution dist) {
        if (dist == null || dist.counts() == null) {
            return 0;
        }
        return dist.counts()
                .values()
                .stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }
}