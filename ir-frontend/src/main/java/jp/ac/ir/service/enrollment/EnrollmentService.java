package jp.ac.ir.service.enrollment;

import jp.ac.ir.model.dto.enrollment.CategoryItem;
import jp.ac.ir.model.dto.enrollment.GradeDetailResponse;
import jp.ac.ir.model.dto.enrollment.LectureList;
import jp.ac.ir.model.dto.enrollment.SelectionOptionsResponse;
import jp.ac.ir.model.dto.enrollment.YearlyDistribution;
import jp.ac.ir.repository.enrollment.EnrollmentApiClient;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentApiClient enrollmentApiClient;
    private List<LectureList> lecturesCache = null;
    private SelectionOptionsResponse selectionOptionsCache = null;

    public EnrollmentService(EnrollmentApiClient enrollmentApiClient) {
        this.enrollmentApiClient = enrollmentApiClient;
    }


    public List<CategoryItem> getCategories(){
        return enrollmentApiClient.getCategoriesResponse().categories();
    }


    private SelectionOptionsResponse getSelectionOptions() {
        if (selectionOptionsCache == null) {
            selectionOptionsCache = enrollmentApiClient.getSelectionOptionsResponse();
        }
        return selectionOptionsCache;
    }


    public List<String> getDisplayTargetGrades() {
        SelectionOptionsResponse response = getSelectionOptions();
        List<String> grades = response != null && response.grades() != null
                ? response.grades()
                : List.of();
        return grades.stream()
                .map(raw -> raw.equals("全体") ? "全体" : raw )
                .distinct()
                .sorted((a, b) -> {
                    if (a.equals("全体")) return -1;
                    if (b.equals("全体")) return 1;
                    return a.compareTo(b);
                })
                .toList();
    }


    public List<String> getAvailableDepartments() {
        SelectionOptionsResponse response = getSelectionOptions();
        return response != null && response.departments() != null
                ? response.departments()
                : List.of();
    }


    public List<String> getAvailableCategories() {
        SelectionOptionsResponse response = getSelectionOptions();
        return response != null && response.categories() != null
                ? response.categories()
                : List.of();
    }


    public List<LectureList> getLectures() {
        if (lecturesCache == null) {
            jp.ac.ir.model.dto.enrollment.LectureListResponse response = enrollmentApiClient.getAllLecturesResponse();
            lecturesCache = response != null && response.lectures() != null
                    ? response.lectures()
                    : List.of();
        }
        return lecturesCache;
    }


    public GradeDetailResponse getLectureDetail(String lectureName) {
        if (lectureName == null || lectureName.isBlank()) {
            return null;
        }

        return enrollmentApiClient.getGradeDetailResponse(
                lectureName.trim()
        );
    }
    public GradeDetailResponse getLectureDetailByCourseId(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }

        return enrollmentApiClient.getGradeDetailResponseByCourseId(courseId.trim());
    }

    public List<LectureList> getLecturesFiltered(
            String grade,
            String department,
            String category,
            String searchText
    ) {
        String normalized = searchText == null ? "" : searchText.trim().toLowerCase();

        return getLectures().stream()
                .filter(l -> matchGrade(l, grade))
                .filter(l -> matchDepartment(l, department))
                .filter(l -> matchCategory(l, category))
                .filter(l -> matchSearchText(l, normalized))
                .collect(Collectors.toList());
    }


    public Map<String, String> getPercentages(YearlyDistribution dist) {
        Map<String, String> result = new LinkedHashMap<>();

        if (dist == null || dist.percentages() == null) {
            return result;
        }

        for (Map.Entry<String, Double> entry : dist.percentages().entrySet()) {
            Double value = entry.getValue() != null ? entry.getValue() : 0.0;
            result.put(entry.getKey(), String.format("%.1f%%", value));
        }

        return result;
    }


    // =========================================================
    // private helper
    // =========================================================

    private boolean matchGrade(LectureList lecture, String grade) {
        if (grade == null || grade.isBlank() || grade.equals("全体")) {
            return true;
        }

        String normalized = grade.replace("年生", "").trim();
        return lecture.targetGrade() != null &&
                lecture.targetGrade().trim().equals(normalized);
    }


    private boolean matchDepartment(LectureList lecture, String department) {
        if (department == null || department.isBlank() || department.equals("全体")) {
            return true;
        }

        return lecture.targetDepartment() != null &&
                lecture.targetDepartment().trim().equals(department.trim());
    }


    private boolean matchCategory(LectureList lecture, String category) {
        if (category == null || category.isBlank() || category.equals("全体")) {
            return true;
        }
        return lecture.category() != null &&
                lecture.category().trim().equals(category.trim());
    }


    private boolean matchSearchText(LectureList lecture, String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return true;
        }
        return (lecture.lectureName() != null &&
                lecture.lectureName().toLowerCase().contains(searchText))
                ||
                (lecture.lectureTeacher() != null &&
                        lecture.lectureTeacher().toLowerCase().contains(searchText));
    }
}
