package jp.ac.ir.model.dto.questionnaire;

public record OptionDto (
        String label,
        int count,
        Integer option_order,
        Integer order
){}
