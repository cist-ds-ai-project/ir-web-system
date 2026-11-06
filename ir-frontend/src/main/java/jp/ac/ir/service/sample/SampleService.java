//package jp.ac.ir.service.sample;
//
//import jp.ac.ir.repository.sample.SampleApiClient;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * サンプルデータの加工を行う Service【サンプル】
// */
//@Service
//public class SampleService {
//
//    private final SampleApiClient sampleApiClient;
//
//    public SampleService(SampleApiClient sampleApiClient) {
//        this.sampleApiClient = sampleApiClient;
//    }
//
//
//    /**
//     * 学科別平均GPAをグラフ用のデータ形式に変換する
//     * <p>
//     * SampleDepartmentGpa {departmentName, avgGpa}
//     * → {name: "学科名", value: 平均GPA} の形式に変換
//     *
//     * @return グラフ表示用のデータリスト
//     */
//
//
//    public List<Map<String, Object>> getDepartmentGpaForChart() {
//        var response = sampleApiClient.getDepartmentGpa();
//        List<SampleDepartmentGpa> gpaList =
//                (response != null && response.data() != null)
//                        ? response.data()
//                        : List.of();
//
//        return gpaList.stream()
//                .map(gpa -> Map.<String, Object>of(
//                        "name", gpa.departmentName(),
//                        "value", gpa.avgGpa()
//                ))
//                .toList();
//    }
//}
