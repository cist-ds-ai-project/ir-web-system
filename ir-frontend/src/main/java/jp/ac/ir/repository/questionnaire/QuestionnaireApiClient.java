package jp.ac.ir.repository.questionnaire;

import jp.ac.ir.model.dto.questionnaire.QuestionnaireResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/questionnaire")
public interface QuestionnaireApiClient {
    @GetExchange("/{lecture_name}")
    QuestionnaireResponse getQuestionnaire(@PathVariable("lecture_name") String lectureName
    );
}
