package jp.ac.ir.model.dto.gpa;

import java.util.List;

public record DepartmentGpaResponse(
        List<DepartmentGpa> departments
) {
}
