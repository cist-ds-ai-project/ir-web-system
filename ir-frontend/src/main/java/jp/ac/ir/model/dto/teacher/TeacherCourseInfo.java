package jp.ac.ir.model.dto.teacher;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TeacherCourseInfo(
        @JsonProperty("course_id") String courseId,
        @JsonProperty("lecture_name") String lectureName,
        Integer year,
        String semester,
        String category,
        @JsonProperty("credits_course") Integer creditsCourse,
        @JsonProperty("student_count") Integer studentCount
) {
}
