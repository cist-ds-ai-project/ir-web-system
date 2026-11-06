package jp.ac.ir.presentation.grade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;

import java.util.List;
import java.util.Map;
@Tag("unified-chart")
@JsModule("./components/unified-chart.tsx")
public class UnifiedChart extends Div {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Map<String,Integer>> comparisonData;
    public UnifiedChart() {
        setWidthFull();
    }
    public void setChartType(String chartType) {
        getElement().setProperty("chartType", chartType != null ? chartType : "bar");
    }

    public void setChartData(Map<String, ? extends Number> data) {
        try{
            String json = objectMapper.writeValueAsString(data != null ? data :Map.of());
            getElement().setProperty("chartDataJson", json);
        }
        catch (JsonProcessingException e){
            throw new RuntimeException("グラフデータのJSON変換に失敗しました",e);
        }
    }
    public void setGradeOrder(List<String> gradeOrder) {
        try {
            String json = objectMapper.writeValueAsString(gradeOrder != null ? gradeOrder : List.of());
            getElement().setProperty("gradeOrderJson", json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("成績ラベル順序のJSON変換に失敗しました", e);
        }
    }
    public void setCountData(Map<String, Integer> counts) {
        try {
            String json = objectMapper.writeValueAsString(counts != null ? counts : Map.of());
            getElement().setProperty("countDataJson", json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("人数データのJSON変換失敗", e);
        }
    }

    public void setMaxValue(int max) {
        getElement().setProperty("maxValue", max);
    }

    public void setChartData(List<Map<String, Object>> data) {
        try {
            String json = objectMapper.writeValueAsString(
                    data != null ? data : List.of()
            );

            getElement().setProperty("chartDataJson", json);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("ヒストグラムデータ変換失敗", e);
        }

    }


    public void setComparisonData(Map<String, Map<String,Integer>> data) {
        try {
            String json = objectMapper.writeValueAsString(
                            data != null ? data : Map.of()
                    );
            getElement().setProperty("comparisonDataJson", json);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "比較データのJSON変換に失敗しました", e);
        }
    }

    public void setTitle(String title) {
        getElement().setProperty("title", title != null ? title : "");
    }

    public void setUnit(String unit) {
        getElement().setProperty(
                "unit",
                unit != null ? unit : "人"
        );
    }

    public void setLabel(String label) {
        getElement().setProperty(
                "label",
                label != null ? label : "人数"
        );
    }

    public void setPercentageUnit(String percentageUnit) {
        getElement().setProperty(
                "percentageUnit",
                percentageUnit != null ? percentageUnit : "%"
        );
    }

    public void setPercentageLabel(String percentageLabel) {
        getElement().setProperty(
                "percentageLabel",
                percentageLabel != null ? percentageLabel : "割合"
        );
    }

    public void setXLabel(String xLabel) {
        getElement().setProperty(
                "xLabel",
                xLabel != null ? xLabel : "GPA"
        );
    }

    public void setYLabel(String yLabel) {
        getElement().setProperty(
                "yLabel",
                yLabel != null ? yLabel : "人数"
        );
    }
}







