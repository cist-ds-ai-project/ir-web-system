package jp.ac.ir.model.dto.enrollment;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CategoryItem(
        @JsonProperty("value") Integer value,
        @JsonProperty("label") String label
) {}