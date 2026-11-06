package jp.ac.ir.repository.enrollment;

import jp.ac.ir.model.dto.enrollment.CategoryListResponse;
import jp.ac.ir.model.dto.enrollment.GradeDetailResponse;
import jp.ac.ir.model.dto.enrollment.LectureListResponse;
import jp.ac.ir.model.dto.enrollment.SelectionOptionsResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/enrollment")
public interface EnrollmentApiClient {

    // 選択肢取得
    @GetExchange("/options")
    SelectionOptionsResponse getSelectionOptionsResponse();

    // 講義一覧取得
    @GetExchange("/lectures")
    LectureListResponse getAllLecturesResponse();

    // 講義詳細（履修者推移）
    @GetExchange("/{lecture_name}")
    GradeDetailResponse getGradeDetailResponse(
            @PathVariable("lecture_name") String lectureName
    );

    // 講義詳細（course_id 指定）
    @GetExchange("/course/{course_id}")
    GradeDetailResponse getGradeDetailResponseByCourseId(
            @PathVariable("course_id") String courseId
    );

    // カテゴリ一覧
    @GetExchange("/categories")
    CategoryListResponse getCategoriesResponse();
}
