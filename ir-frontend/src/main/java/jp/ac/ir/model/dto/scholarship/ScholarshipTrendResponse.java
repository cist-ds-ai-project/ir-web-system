package jp.ac.ir.model.dto.scholarship;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record ScholarshipTrendResponse(
        @JsonProperty("data") List<Map<String, Object>> data,
        @JsonProperty("lines") List<String> lines
){ }