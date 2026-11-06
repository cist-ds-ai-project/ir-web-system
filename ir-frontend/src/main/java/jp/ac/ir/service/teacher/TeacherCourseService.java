package jp.ac.ir.service.teacher;

import jp.ac.ir.model.dto.teacher.TeacherCourseInfo;
import jp.ac.ir.model.dto.teacher.TeacherCourseResponse;
import jp.ac.ir.repository.teacher.TeacherCourseApiClient;
import jp.ac.ir.service.management.SecurityService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class TeacherCourseService {

    private final TeacherCourseApiClient teacherCourseApiClient;
    private final SecurityService securityService;

    public TeacherCourseService(
            TeacherCourseApiClient teacherCourseApiClient,
            SecurityService securityService
    ) {
        this.teacherCourseApiClient = teacherCourseApiClient;
        this.securityService = securityService;
    }

    /**
     * ログイン中の教員が担当している授業を取得
     */
    public List<TeacherCourseInfo> getLoginTeacherCourses() {

        String personId = securityService.getLoginUser().getPersonId();

        TeacherCourseResponse response = teacherCourseApiClient.getTeacherCourses(personId);

        return response != null && response.courses() != null
                ? response.courses()
                : List.of();
    }

    /**
     * ログイン中の教員が担当している授業から、
     * 指定された科目IDの授業を取得
     */
    public TeacherCourseInfo getLoginTeacherCourse(String courseId) {

        return getLoginTeacherCourses().stream()
                .filter(course -> Objects.equals(course.courseId(), courseId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 年度の表示項目を取得
     */
    public List<String> getDisplayYears(
            List<TeacherCourseInfo> courses
    ) {

        List<String> result = new ArrayList<>();
        result.add("全年度");

        courses.stream()
                .map(TeacherCourseInfo::year)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .map(String::valueOf)
                .forEach(result::add);

        return result;
    }

    /**
     * 学期の表示項目を取得
     */
    public List<String> getDisplaySemesters(
            List<TeacherCourseInfo> courses
    ) {

        List<String> result = new ArrayList<>();
        result.add("全体");

        courses.stream()
                .map(TeacherCourseInfo::semester)
                .filter(semester ->
                        semester != null
                                && !semester.isBlank()
                )
                .distinct()
                .forEach(result::add);

        return result;
    }

    /**
     * 区分の表示項目を取得
     */
    public List<String> getDisplayCategories(
            List<TeacherCourseInfo> courses
    ) {

        List<String> result = new ArrayList<>();
        result.add("全体");

        courses.stream()
                .map(TeacherCourseInfo::category)
                .filter(category ->
                        category != null
                                && !category.isBlank()
                )
                .distinct()
                .forEach(result::add);

        return result;
    }

    /**
     * 担当授業を検索条件で絞り込む
     */
    public List<TeacherCourseInfo> getCoursesFiltered(
            List<TeacherCourseInfo> courses,
            String year,
            String semester,
            String category,
            String lectureName
    ) {

        String normalized =
                lectureName == null
                        ? ""
                        : lectureName.trim().toLowerCase();

        return courses.stream()
                .filter(course -> matchYear(course, year))
                .filter(course -> matchSemester(course, semester))
                .filter(course -> matchCategory(course, category))
                .filter(course -> matchLecture(course, normalized))
                .toList();
    }

    private boolean matchYear(
            TeacherCourseInfo course,
            String year
    ) {

        if (year == null
                || year.isBlank()
                || year.equals("全年度")) {

            return true;
        }

        return course.year() != null
                && course.year().toString().equals(year);
    }

    private boolean matchSemester(
            TeacherCourseInfo course,
            String semester
    ) {

        if (semester == null
                || semester.isBlank()
                || semester.equals("全体")) {

            return true;
        }

        return course.semester() != null
                && course.semester().equals(semester);
    }

    private boolean matchCategory(
            TeacherCourseInfo course,
            String category
    ) {

        if (category == null
                || category.isBlank()
                || category.equals("全体")) {

            return true;
        }

        return course.category() != null
                && course.category().equals(category);
    }

    private boolean matchLecture(
            TeacherCourseInfo course,
            String lectureName
    ) {

        if (lectureName == null
                || lectureName.isBlank()) {

            return true;
        }

        return course.lectureName() != null
                && course.lectureName()
                .toLowerCase()
                .contains(lectureName);
    }
}