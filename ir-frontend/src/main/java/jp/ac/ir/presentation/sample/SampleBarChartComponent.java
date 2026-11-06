package jp.ac.ir.presentation.sample;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.react.ReactAdapterComponent;

import java.util.List;
import java.util.Map;

/**
 * 汎用棒グラフコンポーネント（Vaadin ↔ React ラッパー）【サンプル】
 *
 * <p>使い方:
 * <pre>{@code
 * var chart = new SampleBarChartComponent();
 * chart.setTitle("学科別平均GPA");
 * chart.setData(List.of(
 *     Map.of("name", "情報工学科", "value", 2.85),
 *     Map.of("name", "経済学科", "value", 3.12)
 * ));
 * }</pre>
 */
@Tag("sample-bar-chart")
@JsModule("./components/sample/sample-bar-chart.tsx")
public class SampleBarChartComponent extends ReactAdapterComponent implements HasSize {

    public SampleBarChartComponent() {
        setHeight("400px");
        setWidth("100%");
    }

    /**
     * グラフに表示するデータを設定する
     *
     * @param data [{name: カテゴリ名, value: 数値}, ...] 形式のリスト
     */
    public void setData(List<Map<String, Object>> data) {
        setState("data", data);
    }

    /**
     * グラフのタイトルを設定する（省略可）
     *
     * @param title タイトル文字列
     */
    public void setTitle(String title) {
        setState("title", title);
    }
}
