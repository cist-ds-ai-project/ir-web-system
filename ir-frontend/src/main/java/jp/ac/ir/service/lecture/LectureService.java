package jp.ac.ir.service.lecture;

import jp.ac.ir.model.dto.lecture.*;
import jp.ac.ir.repository.lecture.LectureApiClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LectureService {

    private final LectureApiClient lectureApiClient;
    private List<LectureList> lecturesCache = null;
    private SelectionOptionsResponse selectionOptionsCache = null;

    public LectureService(LectureApiClient lectureApiClient) {
        this.lectureApiClient = lectureApiClient;
    }

    /**
     * バックエンドから選択肢の生データを取得しキャッシュする（内部用）
     */
    private SelectionOptionsResponse getSelectionOptions() {
        if (selectionOptionsCache == null) {
            selectionOptionsCache = lectureApiClient.getSelectionOptionsResponse();
        }
        return selectionOptionsCache;
    }

    /**
     * 画面のコンボボックス等に表示するための「対象学年一覧」を取得する
     */
    public List<String> getDisplayTargetGrades() {
        SelectionOptionsResponse response = getSelectionOptions();
        List<String> grades = response != null && response.grades() != null
                ? response.grades()
                : List.of();
        return grades.stream()
                .map(raw -> raw.equals("全体") ? "全体" : raw)
                .distinct()
                .sorted((a, b) -> {
                    if (a.equals("全体")) return -1;
                    if (b.equals("全体")) return 1;
                    return a.compareTo(b);
                })
                .toList();
    }

    /**
     * 画面のコンボボックス等に表示するための「対象学科一覧」を取得する
     */
    public List<String> getDisplayDepartments() {
        SelectionOptionsResponse response = getSelectionOptions();
        List<String> departments = response != null && response.departments() != null
                ? response.departments()
                : List.of();
        return departments.stream()
                .distinct()
                .sorted((a, b) -> {
                    if (a.equals("全体")) return -1;
                    if (b.equals("全体")) return 1;
                    return a.compareTo(b);
                })
                .toList();
    }

    /**
     * 画面のコンボボックス等に表示するための「科目区分（必修/選択）一覧」を取得する
     */
    public List<String> getDisplayCategories() {
        SelectionOptionsResponse response = getSelectionOptions();
        List<String> categories = response != null && response.categories() != null
                ? response.categories()
                : List.of();
        return categories.stream()
                .distinct()
                .sorted((a, b) -> {
                    if (a.equals("全体")) return -1;
                    if (b.equals("全体")) return 1;
                    return a.compareTo(b);
                })
                .toList();
    }

    /**
     * 画面のコンボボックス等に表示するための「開講学期」を取得する
     */
    public List<String> getDisplaySemesters() {
        SelectionOptionsResponse response = getSelectionOptions();
        List<String> semesters = response != null && response.semesters() != null
                ? response.semesters()
                : List.of();

        // ソートは行わず、Python側から届いた理想の順序をそのまま維持してリスト化
        return semesters.stream()
                .distinct()
                .toList();
    }


    /**
     * すべての講義情報を一括で取得する（キャッシュ対応）
     */
    public List<LectureList> getLectures() {
        if (lecturesCache == null) {
            LectureListResponse response = lectureApiClient.getAllLecturesResponse();
            lecturesCache = response != null && response.lectures() != null
                    ? response.lectures()
                    : List.of();
        }
        return lecturesCache;
    }

    public LectureSyllabusDetail getSyllabusDetailByCourseId(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }

        return lectureApiClient.getSyllabusDetailByCourseId(courseId.trim());
    }

    /**
     * 画面から指定された条件（学年、学科、区分、検索テキスト）に基づいて
     * キャッシュ内の科目一覧を絞り込んで返す
     */
    public List<LectureList> getLecturesFiltered(
            String grade,
            String department,
            String category,
            String semester,
            String searchText
    ) {
        String normalized = searchText == null ? "" : searchText.trim().toLowerCase();

        return getLectures().stream()
                .filter(l -> matchGrade(l, grade))
                .filter(l -> matchDepartment(l, department))
                .filter(l -> matchCategory(l, category))
                .filter(l -> matchSearchText(l, normalized))
                .filter(l -> matchSemester(l, semester))
                .collect(Collectors.toList());
    }

    // =========================================================
    // フィルター（絞り込みロジック）
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

    private boolean matchSemester(LectureList lecture, String semester) {
        if (semester == null || semester.isBlank() || semester.equals("全体")) {
            return true;
        }
        return lecture.availableSemester() != null &&
                lecture.availableSemester().trim().equalsIgnoreCase(semester.trim());
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
