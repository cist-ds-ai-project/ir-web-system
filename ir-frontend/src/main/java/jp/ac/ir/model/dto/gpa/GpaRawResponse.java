package jp.ac.ir.model.dto.gpa;

import java.util.List;

public record GpaRawResponse(
        List<Double> gpas
) {
}
