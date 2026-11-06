//package jp.ac.ir.presentation.sample;
//
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.router.Menu;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//import jp.ac.ir.presentation.MainLayout;
////import jp.ac.ir.service.sample.SampleService;
//
///**
// * 学科別平均GPA棒グラフを表示するサンプルビュー
// *
// * データの流れ:
// *   Python API → SampleApiClient → SampleService → このView → SampleBarChartComponent → React
// */
//@Route(value = "sample/gpa-by-department", layout = MainLayout.class)
//@PageTitle("学科別平均GPA")
//@Menu(order = 2, icon = "vaadin:bar-chart", title = "学科別平均GPA")
//public class SampleGpaView extends VerticalLayout {
//
//    public SampleGpaView(SampleService sampleService) {
//        setSizeFull();
//
//        // グラフコンポーネントを作成
//        var chart = new SampleBarChartComponent();
//        chart.setTitle("学科別平均GPA");
//
//        // APIからデータを取得してグラフに渡す
//        var data = sampleService.getDepartmentGpaForChart();
//        chart.setData(data);
//
//        add(chart);
//    }
//}
//package jp.ac.ir.presentation.sample;
//
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.router.Menu;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//import jakarta.annotation.security.RolesAllowed;
//import jp.ac.ir.presentation.MainLayout;
//import jp.ac.ir.service.sample.SampleService;
//
///**
// * 学科別平均GPA棒グラフを表示するサンプルビュー
// *
// * データの流れ:
// *   Python API → SampleApiClient → SampleService → このView → SampleBarChartComponent → React
// */
//@Route(value = "sample/gpa-by-department", layout = MainLayout.class)
//@PageTitle("学科別平均GPA")
//@Menu(order = 2, icon = "vaadin:bar-chart", title = "学科別平均GPA")
//@RolesAllowed({"administrator", "teacher", "student", "commission"})
//public class SampleGpaView extends VerticalLayout {
//
//    public SampleGpaView(SampleService sampleService) {
//        setSizeFull();
//
//        // グラフコンポーネントを作成
//        var chart = new SampleBarChartComponent();
//        chart.setTitle("学科別平均GPA");
//
//        // APIからデータを取得してグラフに渡す
//        var data = sampleService.getDepartmentGpaForChart();
//        chart.setData(data);
//
//        add(chart);
//    }
//}
