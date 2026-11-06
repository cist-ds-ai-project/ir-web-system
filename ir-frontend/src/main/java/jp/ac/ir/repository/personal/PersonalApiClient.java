package jp.ac.ir.repository.personal;

import jp.ac.ir.model.dto.personal.PersonalGpaResponse;
import jp.ac.ir.model.dto.personal.PersonalGradeResponse;
import jp.ac.ir.model.dto.personal.PersonalQuestionnaireResponse;
import jp.ac.ir.model.dto.personal.PersonalSelectionOptionsResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/personal")
public interface PersonalApiClient {

    @GetExchange("/options")
    PersonalSelectionOptionsResponse getOptions();

    @GetExchange("/grades/{person_id}")
    PersonalGradeResponse getPersonalGradesResponse(
            @PathVariable("person_id") String personId
    );

    @GetExchange("/gpa/{person_id}")
    PersonalGpaResponse getPersonalGpaResponse(
            @PathVariable("person_id") String personId
    );

    @GetExchange("/questionnaire/{person_id}/{course_id}")
    PersonalQuestionnaireResponse getPersonalQuestionnaire(
            @PathVariable("person_id") String personId,
            @PathVariable("course_id") String courseId
    );
}