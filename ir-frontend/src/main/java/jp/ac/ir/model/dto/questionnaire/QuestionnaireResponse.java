package jp.ac.ir.model.dto.questionnaire;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record QuestionnaireResponse(
        @JsonProperty("lecture_name")
        String lectureName,
        List<QuestionDto> questions
) {}
