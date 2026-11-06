package jp.ac.ir.model.dto.personal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PersonalQuestionnaireResponse(
        @JsonProperty("question_id") String courseId,
        @JsonProperty("questionnaire") List<PersonalQuestionnaire>  questionnaire,
        @JsonProperty("lecture_name") String lectureName
) {
}
