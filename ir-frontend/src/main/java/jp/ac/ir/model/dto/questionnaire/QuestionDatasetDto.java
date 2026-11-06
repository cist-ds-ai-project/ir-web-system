package jp.ac.ir.model.dto.questionnaire;

import java.util.List;

public record QuestionDatasetDto(
        String year,
        List<OptionDto> options,
        Integer count,
        List<String> answers
) {}
