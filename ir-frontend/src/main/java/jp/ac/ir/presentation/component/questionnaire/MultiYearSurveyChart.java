package jp.ac.ir.presentation.component.questionnaire;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.react.ReactAdapterComponent;

import java.util.List;
import java.util.Map;

@Tag("multi-year-survey-chart")
@JsModule("./components/questionnaire/band-graph.tsx")
public class MultiYearSurveyChart extends ReactAdapterComponent {

    private static final ObjectMapper mapper =
            new ObjectMapper();

    public void setDataset(
            Object dataset
    ) {

        try {

            String json =
                    mapper.writeValueAsString(
                            dataset
                    );

            getElement()
                    .setProperty(
                            "surveyDataJson",
                            json
                    );

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setQuestionText(String text){

        getElement()
                .setProperty(
                        "questionText",
                        text != null ? text : ""
                );
    }

    public void setQuestionId(String id){

        getElement()
                .setProperty(
                        "questionId",
                        id != null ? id : ""
                );
    }
}