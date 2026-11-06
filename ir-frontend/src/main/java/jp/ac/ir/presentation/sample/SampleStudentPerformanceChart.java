package jp.ac.ir.presentation.sample;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import java.util.List;
import java.util.Map;

@Tag("sample-student-performance-chart")
@JsModule("./components/sample/sample-student-performance-chart.tsx")
public class SampleStudentPerformanceChart extends ReactAdapterComponent implements HasSize {

    public SampleStudentPerformanceChart() {
        setHeight("400px");
        setWidth("100%");
    }

    public void setData(List<Map<String, Object>> data) {
        setState("data", data);
    }
}
