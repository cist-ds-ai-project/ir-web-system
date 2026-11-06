package jp.ac.ir.presentation.scholarship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;

import java.util.Map;

@Tag("pie-chart")
@JsModule("./components/Pie-chart.tsx")
public class ScholarshipPieChart extends Div {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ScholarshipPieChart() {
        setWidthFull();
    }

    public void setChartData(Map<String, ? extends Number> data) {
        try {
            String json = objectMapper.writeValueAsString(data != null ? data : Map.of());
            getElement().setProperty("chartDataJson", json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("グラフデータのJSON変換に失敗しました", e);
        }
    }

    public void setTitle(String title) {
        getElement().setProperty("title", title != null ? title : "");
    }
}
