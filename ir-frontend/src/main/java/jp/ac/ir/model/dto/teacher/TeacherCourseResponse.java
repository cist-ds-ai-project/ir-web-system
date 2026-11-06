package jp.ac.ir.model.dto.teacher;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TeacherCourseResponse(
        @JsonProperty("person_id") String personId,
        List<TeacherCourseInfo> courses
) {
}
