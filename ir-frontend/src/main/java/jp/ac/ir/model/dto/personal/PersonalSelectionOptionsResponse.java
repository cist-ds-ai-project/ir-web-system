package jp.ac.ir.model.dto.personal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PersonalSelectionOptionsResponse(
        List<Integer> years,
        List<String> semesters,
        List<String> categories,
        @JsonProperty("grade") List<String> grades
) {
}
