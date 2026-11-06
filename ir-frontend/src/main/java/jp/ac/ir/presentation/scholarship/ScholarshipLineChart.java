package jp.ac.ir.presentation.scholarship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;

import java.util.List;
import java.util.Map;

@Tag("line-chart")
@JsModule("./components/Line-chart-adapter.tsx")
public class ScholarshipLineChart extends Div {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ScholarshipLineChart() {
        setWidthFull();
    }

    public void setChartData(List<Map<String, Object>> data, List<Map<String, String>> lines) {
        try {
            String dataJson = objectMapper.writeValueAsString(data != null ? data : List.of());
            String linesJson = objectMapper.writeValueAsString(lines != null ? lines : List.of());

            getElement().setProperty("chartDataJson", dataJson);
            getElement().setProperty("lineConfigJson", linesJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("グラフデータのJSON変換に失敗しました", e);
        }
    }
}