package jp.ac.ir.model.dto.grade;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GradeDetailResponse (
    @JsonProperty("lecture_name") String lectureName,
    @JsonProperty("distributions") List<YearlyDistribution> distributions
){}
