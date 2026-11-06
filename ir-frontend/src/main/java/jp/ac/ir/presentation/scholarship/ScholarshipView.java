package jp.ac.ir.presentation.scholarship;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.model.dto.scholarship.ScholarshipResponse;
import jp.ac.ir.model.dto.scholarship.ScholarshipTrendResponse;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.component.notification.ErrorNotification;
import jp.ac.ir.service.scholarship.ScholarshipService;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@PageTitle("奨学金")
@Route(value = "scholarship", layout = MainLayout.class)
@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
public class ScholarshipView extends VerticalLayout implements BeforeEnterObserver {

    private final ScholarshipService scholarshipService;

    private final RadioButtonGroup<String> displayModeSelect = new RadioButtonGroup<>("表示形式");
    private final CheckboxGroup<String> typeSelect = new CheckboxGroup<>("種別");
    private final RadioButtonGroup<String> academicYearSelect = new RadioButtonGroup<>("対象学年");
    // 入学年度フィルタ: 未実装（バックエンドの/trendが年度絞り込みに未対応のため一旦非表示）。再度有効化する際はコメントを外すこと。
    // private final RadioButtonGroup<String> enrollmentYearSelect = new RadioButtonGroup<>("入学年度");
    private final RadioButtonGroup<String> facultySelect = new RadioButtonGroup<>("対象学部");
    private final RadioButtonGroup<String> departmentSelect = new RadioButtonGroup<>("対象学科");

    private final ScholarshipPieChart pieChart = new ScholarshipPieChart();
    private final ScholarshipLineChart lineChart = new ScholarshipLineChart();

    private final VerticalLayout content = new VerticalLayout();

    private static final String[] CHART_COLORS = {
            "#135FA8", "#E67E22", "#27AE60", "#8E44AD", "#F1C40F", "#E74C3C", "#34495E"
    };

    public ScholarshipView(ScholarshipService scholarshipService) {
        this.scholarshipService = scholarshipService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("奨学金情報"));

        displayModeSelect.setItems("受給割合", "受給者推移");
        displayModeSelect.setValue("受給割合");

        // enrollmentYearSelect.setVisible(false);
        // enrollmentYearSelect.setEnabled(true);
        lineChart.setVisible(false);

        displayModeSelect.addValueChangeListener(e -> {
            boolean isTrendMode = "受給者推移".equals(e.getValue());

            academicYearSelect.setVisible(!isTrendMode);
            // enrollmentYearSelect.setVisible(isTrendMode);

            pieChart.setVisible(!isTrendMode);
            lineChart.setVisible(isTrendMode);

            updateChart();
        });

        typeSelect.addValueChangeListener(event -> {
            Set<String> newSelection = event.getValue();
            Set<String> oldSelection = event.getOldValue();

            if (newSelection.isEmpty()) {
                typeSelect.setValue(Set.of("全体"));
                return;
            }

            boolean wholeIsNewlySelected = newSelection.contains("全体") && !oldSelection.contains("全体");
            boolean otherIsSelectedWithWhole = newSelection.contains("全体") && newSelection.size() > 1;

            if (wholeIsNewlySelected) {
                typeSelect.setValue(Set.of("全体"));
            } else if (otherIsSelectedWithWhole) {
                typeSelect.setValue(newSelection.stream()
                        .filter(item -> !item.equals("全体"))
                        .collect(Collectors.toSet()));
            } else {
                updateChart();
            }
        });

        facultySelect.addValueChangeListener(e -> {
            loadDepartments(e.getValue());
            updateChart();
        });

        academicYearSelect.addValueChangeListener(e -> updateChart());
        departmentSelect.addValueChangeListener(e -> updateChart());

        VerticalLayout filters = new VerticalLayout(displayModeSelect, typeSelect, academicYearSelect, /* enrollmentYearSelect, */ facultySelect, departmentSelect);
        filters.setSpacing(false);
        filters.setPadding(false);

        content.add(filters, pieChart, lineChart);
        add(content);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        loadInitialOptions();
    }

    private void loadInitialOptions() {
        try {
            List<String> types = scholarshipService.getTypes();
            if (types != null && !types.isEmpty()) {
                typeSelect.setItems(types);
                typeSelect.setValue(Set.of(types.get(0)));
            }

            List<String> academicYears = scholarshipService.getAcademicYears();
            if (academicYears != null && !academicYears.isEmpty()) {
                academicYearSelect.setItems(academicYears);
                academicYearSelect.setValue(academicYears.get(0));
            }

            // List<String> enrollmentYears = scholarshipService.getEnrollmentDates();
            // if (enrollmentYears != null && !enrollmentYears.isEmpty()) {
            //     enrollmentYearSelect.setItems(enrollmentYears);
            //     enrollmentYearSelect.setValue(enrollmentYears.get(0));
            // }

            List<String> faculties = scholarshipService.getFaculties();
            if (faculties != null && !faculties.isEmpty()) {
                facultySelect.setItems(faculties);
                facultySelect.setValue(faculties.get(0));
                loadDepartments(faculties.get(0));
            } else {
                loadDepartments(null);
            }
        } catch (Exception e) {
            new ErrorNotification("選択肢の読み込みに失敗しました。").open();
        }
    }

    private void loadDepartments(String faculty) {
        if (faculty == null) {
            departmentSelect.setItems(Collections.emptyList());
            departmentSelect.setValue(null);
            return;
        }
        try {
            List<String> departments = scholarshipService.getDepartments(faculty);
            departmentSelect.setItems(departments);
            if (departments != null && !departments.isEmpty()) {
                departmentSelect.setValue(departments.get(0));
            } else {
                departmentSelect.setValue(null);
            }
        } catch (Exception e) {
            new ErrorNotification("学科の読み込みに失敗しました。").open();
            departmentSelect.setItems(Collections.emptyList());
        }
    }

    private void updateChart() {
        boolean isTrendMode = "受給者推移".equals(displayModeSelect.getValue());

        if (isTrendMode) {
            updateLineChart();
        } else {
            updatePieChart();
        }
    }

    private void updatePieChart() {
        String academicYear = academicYearSelect.getValue();
        String faculty = facultySelect.getValue();
        String department = departmentSelect.getValue();

        List<String> academicYearParam = academicYear != null ? List.of(academicYear) : Collections.emptyList();
        List<String> facultyParam = faculty != null ? List.of(faculty) : Collections.emptyList();
        List<String> departmentParam = department != null ? List.of(department) : Collections.emptyList();

        try {
            ScholarshipResponse scholarship = scholarshipService.getScholarshipDistribution(null, academicYearParam, facultyParam, departmentParam);

            if (scholarship != null) {
                List<String> apiLabels = scholarship.labels();
                List<Integer> apiData = scholarship.data();

                Map<String, Integer> fullDataMap = new LinkedHashMap<>();
                if (apiLabels != null && apiData != null) {
                    for (int i = 0; i < apiLabels.size(); i++) {
                        fullDataMap.put(apiLabels.get(i), apiData.get(i));
                    }
                }

                Set<String> selectedTypes = typeSelect.getValue();
                Map<String, Integer> chartData = new LinkedHashMap<>();

                if (selectedTypes.isEmpty() || selectedTypes.contains("全体")) {
                    chartData.putAll(fullDataMap);
                } else {
                    for (String type : selectedTypes) {
                        if (fullDataMap.containsKey(type)) {
                            chartData.put(type, fullDataMap.get(type));
                        }
                    }
                }
                pieChart.setChartData(chartData);
            } else {
                pieChart.setChartData(Map.of());
            }
        } catch (HttpClientErrorException.NotFound e) {
            pieChart.setChartData(Map.of());
        } catch (Exception e) {
            new ErrorNotification("奨学金情報の取得中にエラーが発生しました。").open();
            pieChart.setChartData(Map.of());
        }
    }

    private void updateLineChart() {
        String faculty = facultySelect.getValue();
        String department = departmentSelect.getValue();

        List<String> facultyParam = faculty != null ? List.of(faculty) : Collections.emptyList();
        List<String> departmentParam = department != null ? List.of(department) : Collections.emptyList();

        // ↓↓↓ デバッグ用ログ（原因特定用。原因が分かったら削除してください） ↓↓↓
        System.out.println("[Scholarship/trend] request faculty=" + facultyParam
                + " department=" + departmentParam + " type=null");
        // ↑↑↑ デバッグ用ログ ↑↑↑

        try {
            ScholarshipTrendResponse trendResponse = scholarshipService.getScholarshipTrend(null, facultyParam, departmentParam);

            // ↓↓↓ デバッグ用ログ ↓↓↓
            System.out.println("[Scholarship/trend] response="
                    + (trendResponse == null ? "null" :
                    "data.size=" + (trendResponse.data() == null ? "null" : trendResponse.data().size())
                            + ", lines=" + trendResponse.lines()));
            // ↑↑↑ デバッグ用ログ ↑↑↑

            if (trendResponse != null && trendResponse.data() != null) {
                List<Map<String, Object>> apiData = trendResponse.data();

                List<Map<String, Object>> formattedData = new java.util.ArrayList<>();
                for (Map<String, Object> row : apiData) {
                    Map<String, Object> newRow = new LinkedHashMap<>(row);
                    if (newRow.containsKey("enrollment_year")) {
                        newRow.put("name", String.valueOf(newRow.get("enrollment_year")));
                        newRow.remove("enrollment_year");
                    }
                    formattedData.add(newRow);
                }

                Set<String> selectedTypes = typeSelect.getValue();
                List<Map<String, String>> lineConfigs = new java.util.ArrayList<>();

                if (selectedTypes.contains("全体")) {
                    String totalKey = "合計";
                    for (Map<String, Object> row : formattedData) {
                        long total = 0;
                        for (String lineKey : trendResponse.lines()) {
                            Object value = row.get(lineKey);
                            if (value instanceof Number number) {
                                total += number.longValue();
                            }
                        }
                        row.put(totalKey, total);
                    }

                    Map<String, String> config = new LinkedHashMap<>();
                    config.put("key", totalKey);
                    config.put("label", "全体");
                    config.put("color", CHART_COLORS[0]);
                    lineConfigs.add(config);
                } else {
                    int colorIndex = 0;
                    for (String lineKey : trendResponse.lines()) {
                        if (selectedTypes.contains(lineKey)) {
                            Map<String, String> config = new LinkedHashMap<>();
                            config.put("key", lineKey);
                            config.put("label", lineKey);
                            config.put("color", CHART_COLORS[colorIndex % CHART_COLORS.length]);
                            lineConfigs.add(config);
                            colorIndex++;
                        }
                    }
                }

                // ↓↓↓ デバッグ用ログ：typeSelect と lines の名前が一致していないと lineConfigs が空になる ↓↓↓
                System.out.println("[Scholarship/trend] selectedTypes=" + selectedTypes
                        + " -> lineConfigs=" + lineConfigs.size() + "件, formattedData=" + formattedData.size() + "件");
                // ↑↑↑ デバッグ用ログ ↑↑↑

                lineChart.setChartData(formattedData, lineConfigs);
            } else {
                lineChart.setChartData(List.of(), List.of());
            }
        } catch (HttpClientErrorException.NotFound e) {
            // 404: バックエンドに /scholarship/trend が存在しない、またはパラメータ不一致の可能性
            System.out.println("[Scholarship/trend] 404 Not Found: " + e.getMessage());
            lineChart.setChartData(List.of(), List.of());
        } catch (Exception e) {
            // これまで握りつぶされてスタックトレースが出ていなかったため、原因特定のため出力するように変更
            e.printStackTrace();
            new ErrorNotification("推移データの取得中にエラーが発生しました。").open();
            lineChart.setChartData(List.of(), List.of());
        }
    }
}