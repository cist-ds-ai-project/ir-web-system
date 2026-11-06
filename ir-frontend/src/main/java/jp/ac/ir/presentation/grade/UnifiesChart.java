package jp.ac.ir.presentation.grade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;

import java.util.Map;
@Tag("unifies-chart")
@JsModule("./components/unifies-chart.tsx")
public class UnifiesChart extends Div {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public UnifiesChart() {
        setWidthFull();
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
    public void setMultiSeriesChartData(Map<String, Map<String, Double>> data) {
        try {
            String json = objectMapper.writeValueAsString(data != null ? data : Map.of());

            getElement().setProperty("multiSeriesChartDataJson", json);

            // 念のため、単独グラフ用データは空にしておく
            getElement().setProperty("chartDataJson", "{}");
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("複数系列グラフデータのJSON変換に失敗しました", e);
        }
    }

    public void setMaxValue(int max) {
        getElement().setProperty("maxValue", max);
    }
//kokohenkou
    public void setSeriesLabel(String seriesLabel) {
        getElement().setProperty("seriesLabel", seriesLabel != null ? seriesLabel : "履修者数");
    }

    public void setYAxisLabel(String yAxisLabel) {
        getElement().setProperty("yAxisLabel", yAxisLabel != null ? yAxisLabel : "履修者数(人)");
    }
    //koko
}
