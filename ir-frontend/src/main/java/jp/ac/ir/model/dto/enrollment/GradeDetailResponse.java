package jp.ac.ir.model.dto.enrollment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jp.ac.ir.model.dto.enrollment.YearlyDistribution;

import java.util.List;

public record GradeDetailResponse(
    @JsonProperty("lecture_name") String lectureName,
    @JsonProperty("distributions") List<YearlyDistribution> distributions
){}
