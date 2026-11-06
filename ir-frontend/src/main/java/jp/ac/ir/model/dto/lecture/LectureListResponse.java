package jp.ac.ir.model.dto.lecture;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LectureListResponse(
        @JsonProperty("lectures") List<LectureList> lectures
) {}