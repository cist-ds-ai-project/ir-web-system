package jp.ac.ir.model.dto.enrollment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jp.ac.ir.model.dto.enrollment.CategoryItem;

import java.util.List;

public record CategoryListResponse(
        @JsonProperty("categories") List<CategoryItem> categories
) {}