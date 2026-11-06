package jp.ac.ir.model.dto.gpa;

public record GpaStatistics(
        int count,
        double avg,
        double min,
        double max,
        double std
) {
}
