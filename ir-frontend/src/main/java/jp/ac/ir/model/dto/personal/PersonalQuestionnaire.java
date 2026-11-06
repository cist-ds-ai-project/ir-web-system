package jp.ac.ir.model.dto.personal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PersonalQuestionnaire(
     @JsonProperty("question_id") int questionId,
     @JsonProperty("question_title") String questionTitle,
     @JsonProperty("question_text") String questionText,
     @JsonProperty("question_type") String questionType,
     String answer
) {
}
