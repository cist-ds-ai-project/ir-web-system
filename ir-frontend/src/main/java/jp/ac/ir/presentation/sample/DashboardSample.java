package jp.ac.ir.presentation.sample;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.react.ReactAdapterComponent;

@Tag("dashboard-sample")
@JsModule("./components/sample/dashboard-sample.tsx")
public class DashboardSample extends ReactAdapterComponent implements HasSize {

    public DashboardSample() {
        setSizeFull();
    }
}
