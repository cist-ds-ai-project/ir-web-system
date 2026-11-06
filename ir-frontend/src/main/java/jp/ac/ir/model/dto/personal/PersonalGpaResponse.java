package jp.ac.ir.model.dto.personal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PersonalGpaResponse(
        @JsonProperty("person_id") String personId,
        List<PersonalGpa> gpas
) {
}
