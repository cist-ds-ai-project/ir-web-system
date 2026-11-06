//package jp.ac.ir.repository.sample;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import jp.ac.ir.model.dto.sample.SampleDepartmentGpa;
//import org.springframework.web.service.annotation.GetExchange;
//import org.springframework.web.service.annotation.HttpExchange;
//
//import java.util.List;
//
///**
// * Python API（FastAPI）のエンドポイントを宣言的に定義するインターフェース【サンプル】
// */
//@HttpExchange
//public interface SampleApiClient {
//
//    /**
//     * APIレスポンス {@code {"data": [...]}} のラッパー
//     */
//    record DepartmentGpaResponse(
//            @JsonProperty("data") List<SampleDepartmentGpa> data
//    ) {}
//
//    /**
//     * 学科別平均GPAを取得する
//     *
//     * @return 学科別平均GPAのレスポンス
//     */
//    @GetExchange("/sample/gpa-by-department")
//    DepartmentGpaResponse getDepartmentGpa();
//}
