package jp.ac.ir.model.dto.scholarship;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ScholarshipResponse(
        @JsonProperty("labels") List<String> labels,
        @JsonProperty("data") List<Integer> data,
        @JsonProperty("total") Integer total
){ }
