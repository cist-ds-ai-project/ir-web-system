package jp.ac.ir.service.questionnaire;

import jp.ac.ir.model.dto.questionnaire.QuestionnaireResponse;
import jp.ac.ir.repository.questionnaire.QuestionnaireApiClient;
import org.springframework.stereotype.Service;

@Service
public class QuestionnaireService {

    private final QuestionnaireApiClient apiClient;

    public QuestionnaireService(
            QuestionnaireApiClient apiClient
    ) {
        this.apiClient = apiClient;
    }

    public QuestionnaireResponse getDetail(
            String lectureName
    ) {

        if (lectureName == null ||
                lectureName.isBlank()) {
            return null;
        }

        return apiClient.getQuestionnaire(
                lectureName
        );
    }
}