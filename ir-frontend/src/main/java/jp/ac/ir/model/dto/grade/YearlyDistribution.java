package jp.ac.ir.model.dto.grade;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record YearlyDistribution(
        int year,
        @JsonProperty("lecture_teacher") String lectureTeacher,
        Map<String, Integer> counts,
        @JsonProperty("total_students") int totalStudents,
        @JsonProperty("percentages") Map<String, Double> percentages,
        double average,
        double variance
) {
}