package jp.ac.ir.model.dto.enrollment;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LectureList(
        @JsonProperty("course_id") String courseId,
        @JsonProperty("available_semester") Integer availableSemester,
        @JsonProperty("lecture_name") String lectureName,
        @JsonProperty("lecture_teacher") String lectureTeacher,
        @JsonProperty("target_grade") String targetGrade,
        @JsonProperty("target_department") String targetDepartment,
        @JsonProperty("category") String category,
        @JsonProperty("credits_course") Integer creditsCourse
) {
}
