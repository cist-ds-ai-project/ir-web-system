package jp.ac.ir.model.dto.gpa;

import java.util.List;

public record DepartmentGpa(
        String department,
        List<Double> gpas
) {
}
