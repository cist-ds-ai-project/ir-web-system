package jp.ac.ir.presentation.gpa;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.router.*;
import jp.ac.ir.model.dto.gpa.*;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.grade.UnifiedChart;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import jp.ac.ir.presentation.view.TopView;
import jp.ac.ir.service.gpa.GpaService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.awt.*;
import java.util.*;
import java.util.List;

@@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
@Route(value = "/gpa", layout = MainLayout.class)
@PageTitle("GPA統計")
public class GpaView extends VerticalLayout implements BreadcrumbProvider {
    private final GpaService gpaService;

    // フィルタ
    private final RadioButtonGroup<Integer> yearSelect = new RadioButtonGroup<>();
    private final CheckboxGroup<String> departmentSelect = new CheckboxGroup<>();
    private final RadioButtonGroup<String> facultySelect = new RadioButtonGroup<>();
    private final CheckboxGroup<String> gradeSelect = new CheckboxGroup<>();

    private final VerticalLayout chartsArea = new VerticalLayout();
    public GpaView(GpaService gpaService) {

        this.gpaService = gpaService;
        H2 title = new H2("GPA統計");

        initFilters();
        VerticalLayout filterArea = new VerticalLayout();
        filterArea.add(
                createFilterRow("学年", gradeSelect),
                createFilterRow("学部", facultySelect),
                createFilterRow("学科", departmentSelect),
                createFilterRow("開講年度", yearSelect)
        );
        add(title, filterArea, chartsArea);

        updateView();
    }



    private Component createConditionView(List<Integer> years, String faculty, List<String> departments, List<String> grades) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setAlignItems(Alignment.CENTER);

        layout.add(new H4("表示条件"));

        layout.add(createTag("学年: " + (grades.isEmpty() ? "全体" : grades.stream()
                                .sorted()
                                .collect(java.util.stream.Collectors.joining(", ")))));

        // 学部
        layout.add(createTag("学部: " + ((faculty == null || faculty.isBlank()) ? "全体" : faculty)));
        // 学科
        layout.add(createTag("学科: " + (departments.isEmpty() ? "全体" : String.join(", ", departments))));
        // 開講年度
        layout.add(createTag("開講年度: " + (years.isEmpty() ? "全年度" : years.get(0))));
        return layout;
    }

    private Span createTag(String text) {
        Span tag = new Span(text);

        tag.getStyle()
                .set("background-color", "#e3f2fd")
                .set("padding", "4px 10px")
                .set("border-radius", "12px")
                .set("font-size", "14px")
                .set("font-weight", "500");

        return tag;
    }

    private void initFilters() {
        GpaSelectionOptionsResponse options = gpaService.getOptions();

        yearSelect.setItems(options.years());
        departmentSelect.setItems(options.departments());

        facultySelect.setItems(options.faculties());
        gradeSelect.setItems(options.grades());

        yearSelect.addClassName("horizontal-group");
        departmentSelect.addClassName("horizontal-group");
        facultySelect.addClassName("horizontal-radio");
        gradeSelect.addClassName("horizontal-radio");

        yearSelect.setValue(Collections.max(options.years()));
        facultySelect.setValue("全体");
        departmentSelect.setValue(Set.of("全体"));
        gradeSelect.setValue(Set.of("全体"));

        yearSelect.addValueChangeListener(e -> updateView());
        facultySelect.addValueChangeListener(e -> updateView());
        gradeSelect.addValueChangeListener(e -> updateView());
        departmentSelect.addValueChangeListener(event -> {

            Set<String> selected = event.getValue();
            if (selected.contains("全体") && selected.size() > 1) {
                Set<String> oldValue = event.getOldValue();

                if (!oldValue.contains("全体")) {
                    departmentSelect.setValue(Set.of("全体"));
                }
                else {
                    selected.remove("全体");
                    departmentSelect.setValue(selected);
                }
            }
            if (departmentSelect.getSelectedItems().isEmpty()) {
                departmentSelect.setValue(Set.of("全体"));
            }
            updateView();
        });
        gradeSelect.addValueChangeListener(event -> {
            Set<String> selected = event.getValue();
            if (selected.contains("全体") && selected.size() > 1) {
                Set<String> oldValue = event.getOldValue();
                if (!oldValue.contains("全体")) {
                    gradeSelect.setValue(Set.of("全体"));
                }
                else {
                    selected.remove("全体");
                    gradeSelect.setValue(selected);
                }
            }
            if (selected.isEmpty()) {
                gradeSelect.setValue(Set.of("全体"));
            }
            updateView();
        });
    }

    private void updateView() {
        chartsArea.removeAll();

        Integer year = yearSelect.getValue();
        List<String> departments = new ArrayList<>(departmentSelect.getSelectedItems());
        String faculty = facultySelect.getValue();
        List<String> grades = new ArrayList<>(gradeSelect.getSelectedItems());
        List<Integer> years = year == null || year == 0 ? List.of() : List.of(year);
        List<String> sortedGrades = new ArrayList<>(grades);
        List<Double> allGpas = gpaService.getRawGpa(new GpaFilter(years, faculty, departments, grades));

        double globalMin = Collections.min(allGpas);
        double globalMax = Collections.max(allGpas);
        sortedGrades.remove("全体");
        sortedGrades.sort(Comparator.comparingInt(g -> Integer.parseInt(g.substring(1))));
        chartsArea.add(createConditionView(years, faculty, departments, grades));
        int overallMax = grades.stream()
                .map(grade -> {
                    GpaFilter tmpFilter =
                            new GpaFilter(
                                    years,
                                    faculty,
                                    departments,
                                    List.of(grade));

                    List<Double> gpas = gpaService.getRawGpa(tmpFilter);
                    List<HistogramBin> bins = gpaService.buildHistogram(gpas, globalMin, globalMax);
                    return bins.stream()
                            .mapToInt(HistogramBin::count)
                            .max()
                            .orElse(0);
                })
                .max(Integer::compareTo)
                .orElse(0);
        overallMax = ((overallMax + 9) / 10) * 10;
        if (sortedGrades.isEmpty()) {
            sortedGrades = List.of("全体");
        }
        for (String grade : sortedGrades){
            if (!"全体".equals(grade)) {
                chartsArea.add(new Hr());
                chartsArea.add(new H2(grade));
            }
            GpaFilter gradeFilter = new GpaFilter(years, faculty, departments, List.of(grade));

            List<Double> gpas = gpaService.getRawGpa(gradeFilter);

            List<HistogramBin> histogramBins = gpaService.buildHistogram(gpas,globalMin,globalMax);

            chartsArea.add(createHistogram("全体", histogramBins,overallMax));
            GpaStatsResponse overallStats = gpaService.getStats(gradeFilter);

            if (overallStats != null &&
                    overallStats.overall() != null) {

                DepartmentStatistics overallDepartment = new DepartmentStatistics("全体", overallStats.overall());

                chartsArea.add(createStatsGrid(List.of(overallDepartment)));
            }

            createDepartmentCards(gradeFilter, overallStats);
        }
    }

    private void createDepartmentCards(GpaFilter filter, GpaStatsResponse overallStats) {
        HorizontalLayout departmentRow = new HorizontalLayout();
        departmentRow.setWidthFull();

        List<DepartmentGpa> deptList = gpaService.getGpaByDepartment(filter);
        List<Double> allGpas = deptList.stream()
                        .flatMap(d -> d.gpas().stream())
                        .toList();
        double globalMin = Collections.min(allGpas);
        double globalMax = Collections.max(allGpas);
        int commonMax = deptList.stream().flatMap(d ->
                        gpaService.buildHistogram(d.gpas(),globalMin,globalMax).stream())
                        .mapToInt(HistogramBin::count)
                        .max()
                        .orElse(0);
        commonMax = ((commonMax + 9) / 10) * 10;

        for (DepartmentGpa d : deptList) {
            VerticalLayout departmentCard = new VerticalLayout();
            departmentCard.setWidth("32%");
            departmentCard.add(new H3(d.department()));

            List<HistogramBin> deptBins = gpaService.buildHistogram(d.gpas(),globalMin,globalMax);

            departmentCard.add(createHistogram("", deptBins, commonMax));

            overallStats.byDepartment()
                    .stream()
                    .filter(stat ->
                            stat.department()
                                    .equals(d.department()))
                    .findFirst()
                    .ifPresent(stat ->
                            departmentCard.add(createStatsGrid(List.of(stat))));
            departmentRow.add(departmentCard);
            departmentRow.expand(departmentCard);
        }
        chartsArea.add(departmentRow);
    }




    private Component createFilterRow(String label, Component component){
            VerticalLayout layout = new VerticalLayout();

            layout.setPadding(false);
            layout.setSpacing(false);

            H2 title = new H2(label);

            title.getStyle().set("font-size", "16px");

            layout.add(title, component);

            return layout;
        }


    private Component createHistogram(String title, List<HistogramBin> histogramBins, Integer max) {

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.add(new H2(title));
        List<Map<String, Object>> histogramData = gpaService.convertToHistogram(histogramBins);
        UnifiedChart chart = new UnifiedChart();
        chart.setChartType("histogram");
        chart.setChartData(histogramData);
        if (max != null) {
            chart.setMaxValue(max);
        }
        chart.setWidthFull();
        chart.setHeight("250px");

        layout.add(chart);

        return layout;
    }

    private Grid<DepartmentStatistics> createStatsGrid(List<DepartmentStatistics> statsList) {

        Grid<DepartmentStatistics> grid = new Grid<>();
        grid.setItems(statsList);
        grid.setAllRowsVisible(true);

        grid.addColumn(DepartmentStatistics::department).setHeader("学科").setAutoWidth(true);
        grid.addColumn(d -> d.stats().count()).setHeader("人数").setAutoWidth(true);
        grid.addColumn(d -> d.stats().avg()).setHeader("平均").setAutoWidth(true);
        grid.addColumn(d -> d.stats().min()).setHeader("最小").setAutoWidth(true);
        grid.addColumn(d -> d.stats().max()).setHeader("最大").setAutoWidth(true);
        grid.addColumn(d -> d.stats().std()).setHeader("標準偏差").setAutoWidth(true);

        return grid;
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        return List.of(BreadcrumbItem.of("/GPA統計", GpaView.class));
    }
}




