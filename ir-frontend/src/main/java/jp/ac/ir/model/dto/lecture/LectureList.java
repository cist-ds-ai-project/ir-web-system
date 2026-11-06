package jp.ac.ir.model.dto.lecture;
import com.fasterxml.jackson.annotation.JsonProperty;


public record LectureList(
    @JsonProperty("course_id") String courseId,
    @JsonProperty("lecture_name") String lectureName,
    @JsonProperty("lecture_teacher") String lectureTeacher,
    @JsonProperty("target_grade") String targetGrade,
    @JsonProperty("target_department") String targetDepartment,
    @JsonProperty("category") String category,
    @JsonProperty("credits_course") Integer creditsCourse,
    @JsonProperty("available_semester") String availableSemester
) {
}
