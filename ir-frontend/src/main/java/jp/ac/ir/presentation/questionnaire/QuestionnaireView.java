package jp.ac.ir.presentation.questionnaire;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.model.dto.questionnaire.QuestionDatasetDto;
import jp.ac.ir.model.dto.questionnaire.QuestionDto;
import jp.ac.ir.model.dto.questionnaire.QuestionnaireResponse;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.component.questionnaire.MultiYearSurveyChart;
import jp.ac.ir.presentation.lecture.LectureSelectView;
import jp.ac.ir.presentation.lecture.LectureTopView;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import jp.ac.ir.service.questionnaire.QuestionnaireService;
import com.vaadin.flow.router.QueryParameters;
import jp.ac.ir.presentation.personal.PersonalDetailView;

import java.util.*;
import java.util.stream.Stream;

@PageTitle("アンケート結果")
@Route(value = "/survey/:lectureName", layout = MainLayout.class)
@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
public class QuestionnaireView extends VerticalLayout implements BeforeEnterObserver, BreadcrumbProvider {

    private static final int BACK_BUTTON_TOP_PX = 160;
    private static final int TEXT_SECTION_MIN_WIDTH_PX = 360;
    private static final int TEXT_SECTION_HEIGHT_PX = 420;

    private final QuestionnaireService service;

    private String lectureName;
    private String courseId;
    private QuestionnaireResponse current;
    private List<QuestionDto> questions = List.of();

    private final CheckboxGroup<String> yearSelect = new CheckboxGroup<>("年度選択");
    private final Div contentWrapper = new Div();
    private final H2 pageTitle = new H2();

    private String from;

    private Map<String, String> personalDetailParams = new LinkedHashMap<>();

    public QuestionnaireView(QuestionnaireService service) {
        this.service = service;

        setSizeFull();
        setPadding(true);

        yearSelect.addValueChangeListener(e -> render());

        contentWrapper.setWidthFull();

        pageTitle.getStyle()
                .set("margin-top", "8px")
                .set("margin-bottom", "16px");

        add(createBackButton(), pageTitle, yearSelect, contentWrapper);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        this.lectureName = event.getRouteParameters()
                .get("lectureName")
                .orElse("");
        this.lectureName = java.net.URLDecoder.decode(
                lectureName,
                java.nio.charset.StandardCharsets.UTF_8
        );
        this.courseId = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("courseId", List.of(""))
                .stream()
                .findFirst()
                .orElse("");
        this.courseId = java.net.URLDecoder.decode(
                courseId,
                java.nio.charset.StandardCharsets.UTF_8
        );


        Map<String, List<String>> queryParams =
                event.getLocation()
                        .getQueryParameters()
                        .getParameters();

        this.from = queryParams
                .getOrDefault("from", List.of(""))
                .stream()
                .findFirst()
                .orElse("");

        if ("personalDetail".equals(from)) {

            personalDetailParams = new LinkedHashMap<>();

            // lectureName は /survey/:lectureName の
            // ルートパラメータなので、queryParams から取得しない
            personalDetailParams.put("lectureName", lectureName);

            List<String> keys = List.of(
                    "courseId",
                    "year",
                    "semester",
                    "lectureTeacher",
                    "category",
                    "creditsCourse",
                    "grading",
                    "gp",
                    "attendance",
                    "lateness",
                    "absence"
            );

            for (String key : keys) {
                String value = queryParams
                        .getOrDefault(key, List.of(""))
                        .stream()
                        .findFirst()
                        .orElse("");

                personalDetailParams.put(key, value);
            }

        }

        questions = List.of();
        contentWrapper.removeAll();

        current = service.getDetail(lectureName);

        if (current == null || current.questions() == null) {
            return;
        }

        questions = mergeQuestionsByTitle(current.questions());
        pageTitle.setText(current.lectureName());

        List<String> years = availableYears();
        yearSelect.setItems(years);

        if (!years.isEmpty()) {
            yearSelect.setValue(Set.of(years.get(0)));
        }

        render();
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        if (lectureName == null || lectureName.isBlank()) {
            return List.of(
                    BreadcrumbItem.of("成績情報", LectureSelectView.class),
                    BreadcrumbItem.of("アンケート", QuestionnaireView.class)
            );
        }

        return List.of(
                BreadcrumbItem.of("成績情報", LectureSelectView.class),
                BreadcrumbItem.of(
                        lectureName,
                        LectureTopView.class,
                        new RouteParameters("lectureName", lectureName),
                        createCourseIdQueryParameters()
                ),
                BreadcrumbItem.of(
                        "アンケート",
                        QuestionnaireView.class,
                        new RouteParameters("lectureName", lectureName)
                )
        );
    }

    private Button createBackButton() {
        Button backButton = new Button("戻る", e -> navigateBack());
        backButton.setThemeName("tertiary");
        backButton.getStyle()
                .set("position", "fixed")
                .set("top", BACK_BUTTON_TOP_PX + "px")
                .set("right", "16px")
                .set("z-index", "1000");

        return backButton;
    }

    private void render() {
        contentWrapper.removeAll();

        if (questions.isEmpty()
                || yearSelect.getValue() == null
                || yearSelect.getValue().isEmpty()) {
            return;
        }

        for (QuestionDto question : questions) {
            Component block = createQuestionBlock(question);

            if (block != null) {
                contentWrapper.add(block);
            }
        }
    }

    private Component createQuestionBlock(QuestionDto question) {
        VerticalLayout box = new VerticalLayout();

        box.setWidthFull();
        box.setPadding(false);
        box.setSpacing(true);

        box.getStyle()
                .set("border", "1px solid #ddd")
                .set("border-radius", "10px")
                .set("padding", "16px")
                .set("margin-bottom", "16px");

        H3 questionTitle = new H3(
                question.question_Title() + " : " + question.questionText()
        );

        questionTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "1.1rem");

        Component content = createQuestionContent(question);
        if (content == null) {
            return null;
        }

        box.add(questionTitle, content);

        return box;
    }

    private Component createQuestionContent(QuestionDto question) {
        return switch (question.question_type()) {
            case "choice" -> createChoiceChart(question);
            case "text" -> createTextList(question);
            default -> null;
        };
    }

    private void navigateBack() {

        if ("personalDetail".equals(from)) {

            UI.getCurrent().navigate(
                    PersonalDetailView.class,
                    QueryParameters.simple(personalDetailParams)
            );

            return;
        }

        if ("questionnaire".equals(from)) {

            UI.getCurrent().navigate(
                    QuestionnaireView.class,
                    new RouteParameters(
                            "lectureName",
                            lectureName
                    )
            );

            return;
        }

        if (lectureName == null || lectureName.isBlank()) {
            UI.getCurrent().navigate(LectureSelectView.class);
            return;
        }

        UI.getCurrent().navigate(createLectureTopRoute());
    }

    private Component createChoiceChart(QuestionDto question) {
        MultiYearSurveyChart chart = new MultiYearSurveyChart();

        List<QuestionDatasetDto> filteredDataset = datasetOf(question).stream()
                .filter(row -> yearSelect.getValue().contains(row.year()))
                .toList();

        chart.setDataset(filteredDataset);
        chart.setQuestionText(question.questionText());
        chart.setQuestionId(question.question_Title());

        chart.getElement()
                .getStyle()
                .set(
                        "height",
                        Math.max(180, filteredDataset.size() * 55 + 130) + "px"
                )
                .set("width", "100%");

        return chart;
    }

    private Component createTextList(QuestionDto question) {
        List<Component> yearSections = createTextAnswerYearSections(question);

        Div list = new Div();
        list.setWidthFull();
        list.getStyle()
                .set("display", "grid")
                .set("gap", "12px")
                .set("align-items", "stretch")
                .set("overflow-x", "auto")
                .set("padding-bottom", "8px");

        if (yearSections.isEmpty()) {
            return createEmptyTextAnswerNotice();
        }

        list.getStyle().set(
                "grid-template-columns",
                "repeat(" + yearSections.size() + ", minmax(" + TEXT_SECTION_MIN_WIDTH_PX + "px, 1fr))"
        );

        for (Component section : yearSections) {
            list.add(section);
        }

        return list;
    }

    private List<String> availableYears() {
        return questions.stream()
                .flatMap(q -> datasetOf(q).stream())
                .map(QuestionDatasetDto::year)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    private List<QuestionDto> mergeQuestionsByTitle(List<QuestionDto> sourceQuestions) {
        Map<String, QuestionDto> grouped = new LinkedHashMap<>();

        for (QuestionDto question : sourceQuestions) {
            String key = question.question_Title();
            QuestionDto existing = grouped.get(key);

            if (existing == null) {
                grouped.put(key, sortDatasetByYear(question));
                continue;
            }

            grouped.put(key, new QuestionDto(
                    existing.question_id(),
                    existing.questionText(),
                    existing.question_type(),
                    existing.question_Title(),
                    mergeDatasets(existing.dataset(), question.dataset())
            ));
        }

        return new ArrayList<>(grouped.values());
    }

    private QuestionDto sortDatasetByYear(QuestionDto question) {
        return new QuestionDto(
                question.question_id(),
                question.questionText(),
                question.question_type(),
                question.question_Title(),
                mergeDatasets(List.of(), question.dataset())
        );
    }

    private List<QuestionDatasetDto> mergeDatasets(
            List<QuestionDatasetDto> first,
            List<QuestionDatasetDto> second
    ) {
        List<QuestionDatasetDto> merged = new ArrayList<>();

        if (first != null) {
            merged.addAll(first);
        }

        if (second != null) {
            merged.addAll(second);
        }

        return merged.stream()
                .sorted(Comparator.comparing(QuestionDatasetDto::year).reversed())
                .toList();
    }

    private List<String> selectedYearsFor(QuestionDto question) {
        return datasetOf(question).stream()
                .map(QuestionDatasetDto::year)
                .filter(year -> yearSelect.getValue().contains(year))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    private List<Component> createTextAnswerYearSections(QuestionDto question) {
        List<Component> yearSections = new ArrayList<>();

        for (String year : selectedYearsFor(question)) {
            List<String> answers = collectAnswersByYear(question, year);

            if (!answers.isEmpty()) {
                yearSections.add(createTextAnswerYearSection(year, answers));
            }
        }

        return yearSections;
    }

    private String createLectureTopRoute() {
        String route = "lecture/" + java.net.URLEncoder.encode(
                lectureName,
                java.nio.charset.StandardCharsets.UTF_8
        );
        if (courseId != null && !courseId.isBlank()) {
            route += "?courseId=" + java.net.URLEncoder.encode(
                    courseId,
                    java.nio.charset.StandardCharsets.UTF_8
            );
        }
        return route;
    }

    private QueryParameters createCourseIdQueryParameters() {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }
        return QueryParameters.simple(Map.of("courseId", courseId));
    }

    private List<String> collectAnswersByYear(
            QuestionDto question,
            String year
    ) {
        return datasetOf(question).stream()
                .filter(row -> Objects.equals(row.year(), year))
                .flatMap(row -> {
                    if (row.answers() == null) {
                        return Stream.empty();
                    }

                    return row.answers().stream();
                })
                .map(this::normalizeAnswer)
                .filter(answer -> !answer.isEmpty())
                .toList();
    }

    private String normalizeAnswer(String answer) {
        if (answer == null) {
            return "";
        }

        return answer
                .replace('\u00A0', ' ')
                .replaceAll("[\\u200B\\u200C\\u200D\\uFEFF]", "")
                .strip();
    }

    private Component createTextAnswerYearSection(String year, List<String> answers) {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setHeight(TEXT_SECTION_HEIGHT_PX + "px");
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle()
                .set("border", "1px solid #ddd")
                .set("box-sizing", "border-box")
                .set("overflow", "hidden")
                .set("margin-top", "8px")
                .set("margin-bottom", "12px");

        String labelText = year;
        if (year.matches("\\d+")) {
            labelText = year + "年度";
        } else if (year.matches("(\\d+)\\((.+)\\)")) {
            labelText = year.replaceAll("(\\d+)\\((.+)\\)", "$1年度($2)");
        } else if (year.matches("(\\d+)（(.+)）")) {
            labelText = year.replaceAll("(\\d+)（(.+)）", "$1年度（$2）");
        }

        Span yearLabel = new Span(labelText);
        yearLabel.setWidthFull();
        yearLabel.getStyle()
                .set("box-sizing", "border-box")
                .set("font-weight", "600")
                .set("padding", "12px 16px")
                .set("border-bottom", "1px solid #ddd")
                .set("background", "#fafafa");

        section.add(yearLabel);

        Div answersWrapper = new Div();
        answersWrapper.setWidthFull();
        answersWrapper.getStyle()
                .set("overflow-y", "auto")
                .set("flex", "1")
                .set("min-height", "0");

        for (String answer : answers) {
            answersWrapper.add(createTextAnswerCell(answer));
        }

        section.add(answersWrapper);

        return section;
    }

    private Component createTextAnswerCell(String answer) {
        Div cell = new Div(new Text(answer == null ? "" : answer));

        cell.setWidthFull();
        cell.getStyle()
                .set("box-sizing", "border-box")
                .set("padding", "12px 16px")
                .set("border-bottom", "1px solid #eee")
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "anywhere")
                .set("line-height", "1.5");

        return cell;
    }

    private Component createEmptyTextAnswerNotice() {
        Div notice = new Div(new Span("コメントはありません"));

        notice.setWidthFull();
        notice.getStyle()
                .set("border", "1px solid #ccc")
                .set("padding", "8px")
                .set("background", "#fafafa");

        return notice;
    }

    private List<QuestionDatasetDto> datasetOf(QuestionDto question) {
        if (question == null || question.dataset() == null) {
            return List.of();
        }

        return question.dataset();
    }
}
