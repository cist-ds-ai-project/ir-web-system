package jp.ac.ir.model.dto.gpa;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GpaStatsResponse(
        GpaStatistics overall,
        @JsonProperty("by_department") List<DepartmentStatistics> byDepartment
) {
}
