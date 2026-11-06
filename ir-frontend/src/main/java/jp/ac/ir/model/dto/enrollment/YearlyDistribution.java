package jp.ac.ir.model.dto.enrollment;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record YearlyDistribution(
        int year,
        @JsonProperty("lecture_teacher") String lectureTeacher,
        Map<String, Integer> counts,
        @JsonProperty("total_students") int totalStudents,
        @JsonProperty("available_student") Integer availableStudent,
        @JsonProperty("enrollment_rate") Double enrollmentRate,
        @JsonProperty("percentages") Map<String, Double> percentages,
        Double average,
        @JsonProperty("standard_deviation") Double standardDeviation
) {
}
