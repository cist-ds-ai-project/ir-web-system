package jp.ac.ir.model.dto.lecture;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LectureSyllabusDetail(
        @JsonProperty("course_id") String courseId,
        @JsonProperty("lecture_name") String lectureName,
        @JsonProperty("number_credits_course") Integer numberCreditsCourse,
        @JsonProperty("target_grade") String targetGrade,
        @JsonProperty("teachers") List<String> teachers,
        @JsonProperty("lecture_about") String lectureAbout,
        @JsonProperty("lecture_goal") String lectureGoal,
        @JsonProperty("study_outside_of_class") String studyOutsideOfClass,
        @JsonProperty("textbook") String textbook,
        @JsonProperty("grading_standard_explain") String gradingStandardExplain,
        @JsonProperty("lecture_plans") List<LecturePlan> lecturePlans,
        @JsonProperty("evaluation_items") List<EvaluationItem> evaluationItems
) {
    public record LecturePlan(
            @JsonProperty("lecture_plan_id") Integer lecturePlanId,
            @JsonProperty("lecture_plan") String lecturePlan
    ) {
    }

    public record EvaluationItem(
            @JsonProperty("evaluation_item_id") Integer evaluationItemId,
            @JsonProperty("evaluation_item_title") String evaluationItemTitle,
            @JsonProperty("evaluation_item_per") Integer evaluationItemPer
    ) {
    }
}
