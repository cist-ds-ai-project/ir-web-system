package jp.ac.ir.model.dto.enrollment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SelectionOptionsResponse(
        @JsonProperty("grades") List<String> grades,
        @JsonProperty("departments") List<String> departments,
        @JsonProperty("categories") List<String> categories
) {}