package jp.ac.ir.model.dto.personal;

public record PersonalGpa(
        int year,
        String semester,
        double gpa
) {
}
