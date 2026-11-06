package jp.ac.ir.model.dto.personal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PersonalGrade(
        @JsonProperty("course_id") String courseId,
        @JsonProperty("lecture_name") String lectureName,
        @JsonProperty("lecture_teacher") String lectureTeacher,
        @JsonProperty("year") Integer year,
        @JsonProperty("category") String category,
        @JsonProperty("credits_course") Integer creditsCourse,
        @JsonProperty("grading") String grading,
        @JsonProperty("gp") float gp,
        @JsonProperty("attendance") Integer attendance,
        @JsonProperty("lateness") Integer lateness,
        @JsonProperty("absence") Integer absence,
        @JsonProperty("missing") Integer missing,
        @JsonProperty("semester") String semester
) {
}