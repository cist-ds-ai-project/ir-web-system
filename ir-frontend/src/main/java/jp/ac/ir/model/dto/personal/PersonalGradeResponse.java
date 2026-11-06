package jp.ac.ir.model.dto.personal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PersonalGradeResponse(
        @JsonProperty("grades") List<PersonalGrade> grades
) {
}