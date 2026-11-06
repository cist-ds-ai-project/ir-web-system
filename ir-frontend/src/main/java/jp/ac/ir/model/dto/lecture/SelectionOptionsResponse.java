package jp.ac.ir.model.dto.lecture;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SelectionOptionsResponse(
        @JsonProperty("grades") List<String> grades,
        @JsonProperty("departments") List<String> departments,
        @JsonProperty("categories") List<String> categories,
        @JsonProperty("semesters") List<String> semesters
) {}