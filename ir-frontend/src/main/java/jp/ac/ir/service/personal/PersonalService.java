package jp.ac.ir.service.personal;

import jp.ac.ir.model.dto.personal.*;
import jp.ac.ir.repository.personal.PersonalApiClient;
import jp.ac.ir.service.management.SecurityService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PersonalService {
    private PersonalSelectionOptionsResponse optionsCache;
    private final PersonalApiClient personalApiClient;
    private final SecurityService securityService;



    public PersonalService(
            PersonalApiClient personalApiClient,
            SecurityService securityService
    ) {
        this.personalApiClient = personalApiClient;
        this.securityService = securityService;
    }

    public PersonalGradeResponse getLoginUserGrades() {
        String personId = securityService.getLoginUser().getPersonId();

        return personalApiClient.getPersonalGradesResponse(personId);
    }

    public PersonalSelectionOptionsResponse getOptions() {

        if (optionsCache == null) {
            optionsCache = personalApiClient.getOptions();
        }

        return optionsCache;
    }

    public Map<String, Integer> getGradeCountsFiltered(
            String year,
            String semester,
            String category,
            String lectureName,
            String personId) {

        return getGradesFiltered(
                year,
                semester,
                category,
                lectureName,
                personId
        ).stream()
                .filter(g -> g.grading() != null)
                .collect(Collectors.groupingBy(
                        PersonalGrade::grading,
                        Collectors.summingInt(g -> 1)
                ));
    }

    public List<String> getDisplayYears(String personID) {

        List<PersonalGrade> grades = getGrades(personID);

        List<String> result = new ArrayList<>();
        result.add("全年度");

        grades.stream()
                .map(PersonalGrade::year)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(String::valueOf)
                .forEach(result::add);;

        return result;
    }

    public List<String> getDisplaySemesters() {

        PersonalSelectionOptionsResponse response = getOptions();

        List<String> semesters =
                response != null && response.semesters() != null
                        ? response.semesters()
                        : List.of();

        List<String> result = new ArrayList<>();
        result.add("全体");

        semesters.stream()
                .distinct()
                .forEach(result::add);

        return result;
    }

    public List<String> getDisplayCategories() {

        PersonalSelectionOptionsResponse response = getOptions();

        List<String> categories =
                response != null && response.categories() != null
                        ? response.categories()
                        : List.of();

        List<String> result = new ArrayList<>();
        result.add("全体");

        categories.stream()
                .distinct()
                .forEach(result::add);

        return result;
    }

    public List<String> getDisplayGrades(){
        PersonalSelectionOptionsResponse response = getOptions();

        if (response == null || response.grades() == null) {
            return List.of();
        }

        return response.grades();
    }

    public List<PersonalGrade> getGrades(String personId) {

        PersonalGradeResponse response =
                personalApiClient.getPersonalGradesResponse(personId);

        return response != null && response.grades() != null
                ? response.grades()
                : List.of();
    }

    public List<PersonalGrade> getGradesFiltered(
            String year,
            String semester,
            String category,
            String lectureName,
            String personId) {

        String normalized =
                lectureName == null ? "" : lectureName.trim().toLowerCase();

        return getGrades(personId).stream()
                .filter(g -> matchYear(g, year))
                .filter(g -> matchSemester(g, semester))
                .filter(g -> matchCategory(g, category))
                .filter(g -> matchLecture(g, normalized))
                .toList();
    }

    public PersonalQuestionnaireResponse getPersonalQuestionnaire(
            String personId,
            String courseId) {

        return personalApiClient.getPersonalQuestionnaire(
                personId,
                courseId
        );
    }

    private boolean matchYear(PersonalGrade grade, String year) {

        if (year == null || year.isBlank() || year.equals("全年度")) {
            return true;
        }

        return grade.year() != null &&
                grade.year().toString().equals(year);
    }

    private boolean matchSemester(PersonalGrade grade, String semester) {

        if (semester == null || semester.isBlank() || semester.equals("全体")) {
            return true;
        }

        return grade.semester() != null &&
                grade.semester().equals(semester);
    }

    private boolean matchCategory(PersonalGrade grade, String category) {

        if (category == null || category.isBlank() || category.equals("全体")) {
            return true;
        }

        return grade.category() != null &&
                grade.category().equals(category);
    }

    private boolean matchLecture(PersonalGrade grade, String lectureName) {

        if (lectureName == null || lectureName.isBlank()) {
            return true;
        }

        return grade.lectureName() != null &&
                grade.lectureName().toLowerCase().contains(lectureName);
    }

    public List<PersonalGrade> getPersonalGrades(String personId) {

        PersonalGradeResponse response = personalApiClient.getPersonalGradesResponse(personId);

        return response != null && response.grades() != null
                ? response.grades()
                : List.of();
    }

    public List<PersonalGpa> getPersonalGpa(String personId) {

        PersonalGpaResponse response = personalApiClient.getPersonalGpaResponse(personId);

        return response != null && response.gpas() != null
                ? response.gpas()
                : List.of();
    }


}