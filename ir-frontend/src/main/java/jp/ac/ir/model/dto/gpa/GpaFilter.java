package jp.ac.ir.model.dto.gpa;
import java.util.List;
public record GpaFilter(
        List<Integer> years,
        String faculty,
        List<String> departments,
        List<String> grades
) {
}
