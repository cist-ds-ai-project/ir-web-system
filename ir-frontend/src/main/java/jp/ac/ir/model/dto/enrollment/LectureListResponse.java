package jp.ac.ir.model.dto.enrollment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jp.ac.ir.model.dto.enrollment.LectureList;

import java.util.List;

public record LectureListResponse(
        @JsonProperty("lectures") List<LectureList> lectures
) {}