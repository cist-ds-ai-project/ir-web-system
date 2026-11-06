package jp.ac.ir.presentation.usermanagement;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class UserManagementButtons extends VerticalLayout {
    private Button userBulkAddButton;
    private Button userAddButton;
    private Button userDeactivateButton;
    private Button userActivateButton;

    public UserManagementButtons() {
        initializeButtons();
        addComponentsToLayout();
    }

    private void initializeButtons() {
        // ユーザーの一括追加ボタン
        this.userBulkAddButton = new Button("一括追加", new Icon(VaadinIcon.PLUS), buttonClickEvent -> {
            UI.getCurrent().navigate(UserBulkAddView.class);
        });
        userBulkAddButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        userBulkAddButton.setAutofocus(true);

        // ユーザーの追加ボタン
        this.userAddButton = new Button("追加", new Icon(VaadinIcon.PLUS), buttonClickEvent -> {
            UI.getCurrent().navigate(UserAddView.class);
        });
        userAddButton.setAutofocus(true);

        // ユーザーの削除ボタン
        this.userDeactivateButton = new Button("無効化", new Icon(VaadinIcon.BAN), buttonClickEvent -> {
            UI.getCurrent().navigate(UserDeactivateView.class);
        });
        userDeactivateButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        this.userActivateButton = new Button("有効化", new Icon(VaadinIcon.PLAY), buttonClickEvent -> {
            UI.getCurrent().navigate(UserActivateView.class);
        });
        userActivateButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
    }

    // 各種コンポーネントを画面に追加
    private void addComponentsToLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout(
                userBulkAddButton,
                userAddButton,
                userDeactivateButton,
                userActivateButton);
        buttonLayout.getStyle().set("flex-wrap", "wrap");
        add(buttonLayout);
    }
}
