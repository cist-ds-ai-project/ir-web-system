package jp.ac.ir.model.dto.gpa;

import java.util.List;

public record GpaSelectionOptionsResponse(
        List<Integer> years,
        List<String> faculties,
        List<String> departments,
        List<String> grades
) {
}
