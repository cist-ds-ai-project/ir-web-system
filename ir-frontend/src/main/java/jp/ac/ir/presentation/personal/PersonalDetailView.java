package jp.ac.ir.presentation.personal;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.model.dto.personal.PersonalQuestionnaire;
import jp.ac.ir.model.dto.personal.PersonalQuestionnaireResponse;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.grade.GradeView;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import jp.ac.ir.presentation.questionnaire.QuestionnaireView;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouteParameters;
import jp.ac.ir.service.management.LoginUser;
import jp.ac.ir.service.management.SecurityService;
import jp.ac.ir.service.personal.PersonalService;
import com.vaadin.flow.router.QueryParameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RolesAllowed("student")
@Route(value = "personal/detail", layout = MainLayout.class)
@PageTitle("授業詳細")
public class PersonalDetailView extends VerticalLayout
        implements BeforeEnterObserver, BreadcrumbProvider {

    private String lectureName;
    private String year;
    private String semester;

    // 授業情報
    private String teacher;
    private String category;
    private String credits;
    private String grading;
    private String gp;
    private String attendance;
    private String lateness;
    private String absence;
    private final LoginUser loginUser;
    private final String personId;
    private  String courseId;

    private final VerticalLayout questionnaireArea = new VerticalLayout();
    private final PersonalService personalService;

    public PersonalDetailView(PersonalService personalService, SecurityService securityService) {
        this.personalService = personalService;
        this.loginUser = securityService.getLoginUser();
        this.personId = loginUser.getPersonId();
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        getStyle()
                .set("background-color", "#ffffff")
                .set("box-sizing", "border-box");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        QueryParameters queryParameters =
                event.getLocation().getQueryParameters();

        Map<String, List<String>> parameters =
                queryParameters.getParameters();

        this.courseId = parameters
                .getOrDefault("courseId", List.of(""))
                .stream()
                .findFirst()
                .orElse("");

        lectureName = getParameter(parameters, "lectureName");
        year = getParameter(parameters, "year");
        semester = getParameter(parameters, "semester");

        teacher = getParameter(parameters, "lectureTeacher");
        category = getParameter(parameters, "category");
        credits = getParameter(parameters, "creditsCourse");
        grading = getParameter(parameters, "grading");
        gp = getParameter(parameters, "gp");
        attendance = getParameter(parameters, "attendance");
        lateness = getParameter(parameters, "lateness");
        absence = getParameter(parameters, "absence");

        createPage();
    }

    private String getParameter(
            Map<String, List<String>> parameters,
            String key) {

        List<String> values = parameters.get(key);

        if (values == null || values.isEmpty()) {
            return "";
        }

        return values.get(0);
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {

        if (lectureName == null || lectureName.isBlank()) {
            return List.of(
                    BreadcrumbItem.of(
                            "個人成績",
                            PersonalView.class
                    ),
                    BreadcrumbItem.of(
                            "授業詳細",
                            PersonalDetailView.class
                    )
            );
        }

        return List.of(
                BreadcrumbItem.of(
                        "個人成績",
                        PersonalView.class
                ),
                BreadcrumbItem.of(
                        lectureName,
                        PersonalDetailView.class
                )
        );
    }

    private void createPage() {

        removeAll();

        createHeader();

        createCourseInfo();

        createGradeInfo();

        createQuestionnaire();

        createNavigation();

        createBackButton();
    }

    private void createHeader() {

        H1 title = new H1("授業詳細");

        HorizontalLayout header = new HorizontalLayout(title);

        header.setAlignItems(Alignment.BASELINE);
        header.setWidthFull();

        add(header);
    }

    private void createCourseInfo() {

        H2 courseTitle = new H2(lectureName);

        courseTitle.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "20px");

        VerticalLayout left = new VerticalLayout();

        left.setPadding(false);
        left.setSpacing(false);
        left.setWidth("50%");

        left.add(
                createInfoRow("担当教員", teacher),
                createInfoRow("年度", year),
                createInfoRow("必選区分", category)
        );

        VerticalLayout right = new VerticalLayout();

        right.setPadding(false);
        right.setSpacing(false);
        right.setWidth("50%");

        right.add(
                createInfoRow("学期", semester),
                createInfoRow("単位", credits)
        );

        HorizontalLayout infoColumns = new HorizontalLayout(left, right);

        infoColumns.setWidthFull();
        infoColumns.setSpacing(true);

        VerticalLayout card = new VerticalLayout(courseTitle, infoColumns);

        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(false);

        styleCard(card);

        add(card);
    }

    private void createGradeInfo() {

        H2 title = new H2("あなたの成績・出席状況");

        title.getStyle()
                .set("margin-top", "15px")
                .set("margin-bottom", "10px");

        HorizontalLayout firstRow = new HorizontalLayout();

        firstRow.setWidthFull();

        firstRow.add(
                createStatCard("評価", grading),
                createStatCard("GP", gp),
                createStatCard("単位", credits + "単位")
        );

        HorizontalLayout secondRow = new HorizontalLayout();

        secondRow.setWidthFull();

        secondRow.add(
                createStatCard("出席", attendance + "回"),
                createStatCard("遅刻", lateness + "回"),
                createStatCard("欠席", absence + "回")
        );

        VerticalLayout section = new VerticalLayout(title, firstRow, secondRow);

        section.setWidthFull();
        section.setPadding(false);
        section.setSpacing(true);

        add(section);
    }


    private VerticalLayout createStatCard(String label, String value) {

        Span labelSpan = new Span(label);

        labelSpan.getStyle()
                .set("font-size", "1rem")
                .set("color", "#555");

        Span valueSpan = new Span(value);

        valueSpan.getStyle()
                .set("font-size", "1.8rem")
                .set("font-weight", "bold")
                .set("color", "#005FDB");

        VerticalLayout card = new VerticalLayout(labelSpan, valueSpan);

        card.setWidth("33.33%");
        card.setMinHeight("120px");
        card.setPadding(true);
        card.setSpacing(false);

        card.getStyle()
                .set("border", "1px solid #d9e2ec")
                .set("border-radius", "8px")
                .set("background-color", "#ffffff")
                .set("box-sizing", "border-box");

        return card;
    }

    private void createNavigation() {

        H2 title = new H2("授業データ");

        title.getStyle()
                .set("margin-top", "20px")
                .set("margin-bottom", "10px");

        HorizontalLayout navigation = new HorizontalLayout();

        navigation.setWidthFull();

        VerticalLayout gradeCard = createNavigationCard(
                        "成績分布を見る",
                        "授業全体の成績分布を確認できます。",
                        e -> openGradeDistribution()
                );

        VerticalLayout questionnaireCard = createNavigationCard(
                        "アンケートを見る",
                        "授業に関するアンケート結果を確認できます。",
                        e -> openQuestionnaire()
                );

        navigation.add(gradeCard, questionnaireCard);

        VerticalLayout section = new VerticalLayout(title, navigation);

        section.setWidthFull();
        section.setPadding(false);
        section.setSpacing(true);

        add(section);
    }

    private VerticalLayout createNavigationCard(String title, String description, com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {

        Span titleSpan = new Span(title);

        titleSpan.getStyle()
                .set("font-size", "1.2rem")
                .set("font-weight", "bold")
                .set("color", "#005FDB");

        Span descriptionSpan = new Span(description);

        descriptionSpan.getStyle()
                .set("color", "#555")
                .set("font-size", "0.95rem");

        Button button = new Button(title);

        button.addClickListener(listener);

        button.getStyle()
                .set("color", "#005FDB");

        VerticalLayout card = new VerticalLayout(titleSpan, descriptionSpan, button);

        card.setWidth("50%");
        card.setMinHeight("150px");
        card.setPadding(true);

        card.getStyle()
                .set("border", "1px solid #d9e2ec")
                .set("border-radius", "8px")
                .set("background-color", "#ffffff")
                .set("box-sizing", "border-box");

        return card;
    }


    private HorizontalLayout createInfoRow(String label, String value) {

        Span labelSpan = new Span(label);

        labelSpan.getStyle()
                .set("font-weight", "bold")
                .set("width", "120px")
                .set("color", "#444");

        Span valueSpan = new Span(value);

        HorizontalLayout row = new HorizontalLayout(labelSpan, valueSpan);

        row.setWidthFull();
        row.setPadding(false);

        row.getStyle()
                .set("padding", "10px 0")
                .set("border-bottom", "1px solid #e5e7eb");

        return row;
    }

    private void styleCard(VerticalLayout card) {

        card.getStyle()
                .set("border", "1px solid #d9e2ec")
                .set("border-radius", "8px")
                .set("background-color", "#ffffff")
                .set("box-sizing", "border-box");
    }

    private void createBackButton() {

        Button backButton = new Button("← 個人成績へ戻る");

        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("personal")));

        add(backButton);
    }


    private void openGradeDistribution() {
        Map<String, String> queryParams =
                new HashMap<>();

        queryParams.put("from", "personalDetail");
        queryParams.put("courseId", courseId);
        queryParams.put("year", year);
        queryParams.put("semester", semester);
        queryParams.put("lectureTeacher", teacher);
        queryParams.put("category", category);
        queryParams.put("creditsCourse", credits);
        queryParams.put("grading", grading);
        queryParams.put("gp", gp);
        queryParams.put("attendance", attendance);
        queryParams.put("lateness", lateness);
        queryParams.put("absence", absence);

        UI.getCurrent().navigate(
                GradeView.class,
                new RouteParameters(
                        "lectureName",
                        lectureName
                ),
                QueryParameters.simple(queryParams)
        );
    }


    private void openQuestionnaire() {
        Map<String, String> queryParams = new HashMap<>();

        queryParams.put("from", "personalDetail");
        queryParams.put("courseId", courseId);
        queryParams.put("year", year);
        queryParams.put("semester", semester);
        queryParams.put("lectureTeacher", teacher);
        queryParams.put("category", category);
        queryParams.put("creditsCourse", credits);
        queryParams.put("grading", grading);
        queryParams.put("gp", gp);
        queryParams.put("attendance", attendance);
        queryParams.put("lateness", lateness);
        queryParams.put("absence", absence);

        UI.getCurrent().navigate(
                QuestionnaireView.class,
                new RouteParameters(
                        "lectureName",
                        lectureName
                ),
                QueryParameters.simple(queryParams)
        );

    }

    private void createQuestionnaire() {

        H2 title = new H2("アンケート振り返り");

        questionnaireArea.removeAll();
        questionnaireArea.setWidthFull();
        questionnaireArea.setPadding(false);
        questionnaireArea.setSpacing(true);

        questionnaireArea.add(title);

        PersonalQuestionnaireResponse response = personalService.getPersonalQuestionnaire(personId, courseId);

        if (response == null || response.questionnaire() == null || response.questionnaire().isEmpty()) {
            questionnaireArea.add(new Span("この授業のアンケート回答はありません。"));
            return;
        }

        for (PersonalQuestionnaire questionnaire : response.questionnaire()) {

            VerticalLayout question = new VerticalLayout();

            Span questionTitle = new Span(questionnaire.questionTitle());

            questionTitle.getStyle()
                    .set("font-weight", "bold")
                    .set("font-size", "1rem");

            Span questionText = new Span(questionnaire.questionText());

            String answerText = questionnaire.answer();

            Span answer;
            if (answerText == null || answerText.isBlank()) {
                answer = new Span("回答がありません");
            }
            else {
                answer = new Span("回答：" + answerText);
            }
            answer.getStyle()
                    .set("padding", "10px")
                    .set("background-color", "#f5f7fa")
                    .set("border-radius", "6px");

            question.add(questionTitle, questionText, answer);

            question.setPadding(true);
            question.setSpacing(false);

            question.getStyle()
                    .set("border", "1px solid #d9e2ec")
                    .set("border-radius", "8px");

            questionnaireArea.add(question);
        }
        add(questionnaireArea);
    }
}