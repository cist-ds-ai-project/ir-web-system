package jp.ac.ir.model.dto.questionnaire;

import java.util.List;

public record QuestionDto (
        int question_id,
        String questionText,
        String question_type,
        String question_Title,
        List<QuestionDatasetDto> dataset
){}
