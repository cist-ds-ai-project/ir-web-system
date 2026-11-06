//package jp.ac.ir.model.dto.grade;
//
//
//import jp.ac.ir.service.grade.GradeService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class GradeServiceTest {
//
//    @Autowired
//    private GradeService gradeService;
//
//    // =========================================================
//    // getDisplayTargetGrades
//    // =========================================================
//
//    @Test
//    @DisplayName("学年一覧が取得でき、「全体」が先頭にあること")
//    void 学年一覧が正常に取得できること() {
//        List<String> grades = gradeService.getDisplayTargetGrades();
//
//        assertNotNull(grades, "学年リストがnullです");
//        assertFalse(grades.isEmpty(), "学年リストが空です");
//        assertEquals("全体", grades.get(0), "先頭が「全体」ではありません");
//
//        System.out.println("--- 学年一覧 ---");
//        grades.forEach(System.out::println);
//    }
//
//    @Test
//    @DisplayName("学年が「〇年生」形式に変換されていること（「全体」を除く）")
//    void 学年が年生形式に変換されていること() {
//        List<String> grades = gradeService.getDisplayTargetGrades();
//
//        grades.stream()
//                .filter(g -> !g.equals("全体"))
//                .forEach(g -> assertTrue(
//                        g.endsWith("年生"),
//                        "「" + g + "」が「年生」形式になっていません"
//                ));
//    }
//
//    // =========================================================
//    // getCategories
//    // =========================================================
//
//    @Test
//    @DisplayName("授業区分一覧が取得できること")
//    void 授業区分一覧が正常に取得できること() {
//        List<CategoryItem> categories = gradeService.getCategories();
//
//        assertNotNull(categories, "授業区分リストがnullです");
//        assertFalse(categories.isEmpty(), "授業区分リストが空です");
//
//        CategoryItem first = categories.get(0);
//        assertNotNull(first.value(), "idがnullです");
//        assertNotNull(first.label(), "category_nameがnullです");
//
//        System.out.println("--- 授業区分一覧 ---");
//        categories.forEach(c -> System.out.println(c.value() + " : " + c.label()));
//    }
//
//    // =========================================================
//    // getAvailableDepartments
//    // =========================================================
//
//    @Test
//    @DisplayName("学科一覧が取得でき、「全体」が先頭にあること")
//    void 学科一覧が正常に取得できること() {
//        List<String> departments = gradeService.getAvailableDepartments();
//
//        assertNotNull(departments, "学科リストがnullです");
//        assertFalse(departments.isEmpty(), "学科リストが空です");
//        assertEquals("全体", departments.get(0), "先頭が「全体」ではありません");
//
//        System.out.println("--- 学科一覧 ---");
//        departments.forEach(System.out::println);
//    }
//
//    // =========================================================
//    // getLectures
//    // =========================================================
//
//    @Test
//    @DisplayName("講義一覧が取得でき、各フィールドが揃っていること")
//    void 講義一覧が正常に取得できること() {
//        List<LectureList> lectures = gradeService.getLectures();
//
//        assertNotNull(lectures, "講義リストがnullです");
//        assertFalse(lectures.isEmpty(), "講義リストが空です");
//
//        lectures.stream().limit(3).forEach(l -> {
//            assertNotNull(l.lectureName(),    "lecture_nameがnullです");
//            assertNotNull(l.lectureTeacher(), "lecture_teacherがnullです");
//            assertNotNull(l.targetGrade(),    "target_gradeがnullです");
//            assertNotNull(l.targetDepartment(), "target_departmentがnullです");
//            assertNotNull(l.category(),       "categoryがnullです");
//            assertNotNull(l.creditsCourse(),  "credits_courseがnullです");
//            assertNotEquals("不明", l.lectureTeacher(), "教員名が「不明」になっています");
//
//            System.out.println("--- 講義情報 ---");
//            System.out.println("科目名　　: " + l.lectureName());
//            System.out.println("教員名　　: " + l.lectureTeacher());
//            System.out.println("対象学年　: " + l.targetGrade());
//            System.out.println("対象学科　: " + l.targetDepartment());
//            System.out.println("授業区分　: " + l.category());
//            System.out.println("単位数　　: " + l.creditsCourse());
//            System.out.println();
//        });
//    }
//
//    // =========================================================
//    // getLecturesFiltered
//    // =========================================================
//
//    @Test
//    @DisplayName("フィルタが「全体」の場合、全件返ること")
//    void フィルタ全体で全件返ること() {
//        List<LectureList> all = gradeService.getLectures();
//        List<LectureList> filtered = gradeService.getLecturesFiltered("全体", "全体");
//
//        assertEquals(all.size(), filtered.size(), "「全体」フィルタで件数が変わっています");
//    }
//
//    @Test
//    @DisplayName("学年フィルタをかけると、対象学年のみ返ること")
//    void 学年フィルタが正常に機能すること() {
//        // 実データに合わせて変更してください
//        String targetGrade = "B1";
//        String normalized = targetGrade.replace("年生", "");
//
//        List<LectureList> filtered = gradeService.getLecturesFiltered(targetGrade, "全体");
//
//        assertNotNull(filtered);
//        filtered.forEach(l ->
//                assertEquals(normalized, l.targetGrade(),
//                        "フィルタ外の学年「" + l.targetGrade() + "」が含まれています")
//        );
//
//        System.out.println("--- " + targetGrade + "フィルタ結果: " + filtered.size() + "件 ---");
//    }
//
//    // =========================================================
//    // getLectureDetail
//    // =========================================================
//
//    @Test
//    @DisplayName("存在する講義名で、科目名・教員名・分布リストが取得できること")
//    void 正常な講義名で詳細データが取得できること() {
//        // 実データに合わせて変更してください
//        String targetLecture = "基礎数学入門";
//
//        GradeDetailResponse detail = gradeService.getLectureDetail(targetLecture);
//
//        assertNotNull(detail, "レスポンスがnullです");
//        assertEquals(targetLecture, detail.lectureName(), "科目名が一致しません");
//        assertNotNull(detail.distributions(), "分布リストがnullです");
//        assertFalse(detail.distributions().isEmpty(), "分布データが1件もありません");
//
//        YearlyDistribution latest = detail.distributions().get(0);
//        assertNotNull(latest.lectureTeacher(), "教員名がnullです");
//        assertNotEquals("不明", latest.lectureTeacher(), "教員名が「不明」になっています");
//        assertNotNull(latest.counts(), "countsがnullです");
//        assertTrue(latest.totalStudents() > 0, "total_studentsが0以下です");
//
//        System.out.println("--- 取得成功 ---");
//        System.out.println("科目: " + detail.lectureName());
//        detail.distributions().forEach(d ->
//                System.out.println(d.year() + "年度 / 担当: " + d.lectureTeacher()
//                        + " / 受講者: " + d.totalStudents()
//                        + " / 平均GP: " + d.average())
//        );
//    }
//
//    @Test
//    @DisplayName("存在しない講義名の場合、空の分布リストが返ること")
//    void 存在しない講義名で空データが返ること() {
//        String fakeName = "存在しない幽霊科目";
//
//        GradeDetailResponse detail = gradeService.getLectureDetail(fakeName);
//
//        assertNotNull(detail);
//        assertEquals(fakeName, detail.lectureName());
//        assertTrue(detail.distributions().isEmpty(), "分布リストが空ではありません");
//
//        System.out.println("--- 異常系テスト成功 ---");
//    }
//
//    @Test
//    @DisplayName("nullまたは空文字の場合、nullが返ること")
//    void 空文字でnullが返ること() {
//        assertNull(gradeService.getLectureDetail(null));
//        assertNull(gradeService.getLectureDetail(""));
//        assertNull(gradeService.getLectureDetail("   "));
//    }
//
//    // =========================================================
//    // getPercentages
//    // =========================================================
//
//    @Test
//    @DisplayName("パーセンテージが「xx.xx%」形式で返ること")
//    void パーセンテージが正しい形式で返ること() {
//        // 実データに合わせて変更してください
//        String targetLecture = "基礎数学入門";
//
//        GradeDetailResponse detail = gradeService.getLectureDetail(targetLecture);
//        assertNotNull(detail);
//        assertFalse(detail.distributions().isEmpty());
//
//        YearlyDistribution dist = detail.distributions().get(0);
//        Map<String, String> percentages = gradeService.getPercentages(dist);
//
//        assertFalse(percentages.isEmpty(), "パーセンテージが空です");
//        percentages.forEach((k, v) -> {
//            assertTrue(v.endsWith("%"), "「" + v + "」が%形式ではありません");
//            System.out.println(k + " : " + v);
//        });
//    }
//
//    @Test
//    @DisplayName("nullを渡した場合、空のMapが返ること")
//    void nullで空Mapが返ること() {
//        Map<String, String> result = gradeService.getPercentages(null);
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//    }
//}