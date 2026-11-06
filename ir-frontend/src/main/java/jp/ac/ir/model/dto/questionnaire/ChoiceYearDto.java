package jp.ac.ir.model.dto.questionnaire;

import java.util.List;

public record ChoiceYearDto (
        int year,
        List<OptionDto> options
){}
