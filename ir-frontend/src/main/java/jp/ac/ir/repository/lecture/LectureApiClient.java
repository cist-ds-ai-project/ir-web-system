package jp.ac.ir.repository.lecture;

import jp.ac.ir.model.dto.lecture.LectureListResponse;
import jp.ac.ir.model.dto.lecture.LectureSyllabusDetail;
import jp.ac.ir.model.dto.lecture.SelectionOptionsResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/lecture")
public interface LectureApiClient {

    @GetExchange("/options")
    SelectionOptionsResponse getSelectionOptionsResponse();

    @GetExchange("/all")
    LectureListResponse getAllLecturesResponse();

    @GetExchange("/syllabus/{course_id}")
    LectureSyllabusDetail getSyllabusDetailByCourseId(
            @PathVariable("course_id") String courseId
    );

}
