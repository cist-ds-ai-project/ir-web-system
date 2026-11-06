package jp.ac.ir.presentation.usermanagement;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import jp.ac.ir.service.management.UserManagementService;
import jp.ac.ir.service.management.UsersData;
import java.util.List;


public class UsersDataGrid extends VerticalLayout {
    private RadioButtonGroup<String> rolesRadioButton;
    private RadioButtonGroup<String> availableFilterRadioButton;
    private GridListDataView<UsersData> gridListDataView;
    private final Grid<UsersData> grid;
    private final TextField searchField = new TextField("検索");
    private List<UsersData> allUsers;


    // グリッドにマルチセレクトモードを付けるかどうか判定する
    public enum SelectionMode {
        SINGLE, MULTI
    }

    // コンストラクタ
    public UsersDataGrid(UserManagementService userManagementService, SelectionMode selectionMode) {
        initializeRadioButtons();
        grid = initializeGrid(userManagementService, selectionMode);

        addComponentsToLayout(grid);
        updateLectureGrid();
    }

    // ラジオボタンをまとめて初期化 フィルタ設定
    private void initializeRadioButtons() {
        initializeRoleRadioButton();
        //nameFilter = new TextField("名前検索");
        searchField.setPlaceholder("ユーザー名を入力");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
    }

    // ロールの絞り込みをするラジオボタンの初期化
    private void initializeRoleRadioButton() {
        rolesRadioButton = new RadioButtonGroup<>();
        rolesRadioButton.setItems("全て", "システム管理者", "IR委員会メンバー", "教員", "学生");
        rolesRadioButton.setValue("全て");
        availableFilterRadioButton = new RadioButtonGroup<>();
        availableFilterRadioButton.setItems("全て", "無効のみ", "有効のみ");
        availableFilterRadioButton.setValue("有効のみ");
        rolesRadioButton.addValueChangeListener(e -> updateLectureGrid());
        availableFilterRadioButton.addValueChangeListener(e -> updateLectureGrid());
        searchField.addValueChangeListener(e -> updateLectureGrid());
    }

    // グリッドの初期化
    private Grid<UsersData> initializeGrid(UserManagementService userManagementService, SelectionMode selectionMode) {
        Grid<UsersData> grid = new Grid<>(UsersData.class, false);
        addColumnsToGrid(grid);
        grid.setWidthFull();
        grid.setAllRowsVisible(false);

        // コンストラクタで渡された選択モードに基づいてグリッドを設定
        if (selectionMode == SelectionMode.MULTI) {
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
        } else {
            grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        }
        allUsers = userManagementService.getUsersData().data();
        grid.setItems(allUsers);

        return grid;
    }


    // グリッドの情報を得るメソッド
    public Grid<UsersData> getGrid() {
        return grid;
    }

    // 状態の表示を有効か無効に変更するメソッド
    private String changeIsAvailableValue(boolean isAvailable) {
        if (isAvailable) return "有効";
        return "無効";
    }

    // グリッドにカラムを追加
    private void addColumnsToGrid(Grid<UsersData> grid) {
        grid.addColumn(data -> changeIsAvailableValue(data.is_available())).setHeader("状態").setSortable(true);
        grid.addColumn(UsersData::login_id).setHeader("ログインID").setSortable(true);
        grid.addColumn(UsersData::user_name).setHeader("ユーザーネーム");
        grid.addColumn(UsersData::display_name).setHeader("ロール");
        grid.addColumn(UsersData::created_at).setHeader("作成時間").setSortable(true);
        grid.addColumn(UsersData::deleted_at).setHeader("削除時間").setSortable(true);

        // ユーザー情報変更のためのボタン
        grid.addComponentColumn(usersData -> {
            Button detailButton = new Button("変更");
            detailButton.addClickListener(e -> {
                // 選択したユーザー情報を変更画面に引き渡す
                UI.getCurrent().getSession().setAttribute(UsersData.class, usersData);
                UI.getCurrent().navigate(UserUpdateView.class);
            });
            return detailButton;
        }).setHeader("ユーザーの情報変更");
    }

    // 各種コンポーネントを画面に追加
    private void addComponentsToLayout(Grid<UsersData> grid) {
        add(new H3("名前検索"), searchField);
        add(new H3("ロール"), rolesRadioButton);
        add(availableFilterRadioButton);
        add(grid);
        //add(new H3("学籍番号検索"), studentIdFilter, studentSearchButton, studentGrid);
    }

    private void updateLectureGrid() {
        String searchText = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase();

        List<UsersData> filtered = allUsers;

        // ロール
        if (!"全て".equals(rolesRadioButton.getValue())) {
            filtered = filtered.stream()
                    .filter(user ->
                            user.display_name().equals(rolesRadioButton.getValue()))
                    .toList();
        }

        // 有効/無効
        if ("有効のみ".equals(availableFilterRadioButton.getValue())) {
            filtered = filtered.stream()
                    .filter(UsersData::is_available)
                    .toList();
        } else if ("無効のみ".equals(availableFilterRadioButton.getValue())) {
            filtered = filtered.stream()
                    .filter(user -> !user.is_available())
                    .toList();
        }

        // 検索（ユーザー名）
        if (!searchText.isEmpty()) {
            filtered = filtered.stream()
                    .filter(user ->
                            user.user_name() != null &&
                                    user.user_name().toLowerCase().contains(searchText))
                    .toList();
        }

        grid.setItems(filtered);
    }
}
