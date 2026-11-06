package jp.ac.ir.presentation.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.service.management.UsersService;
import jp.ac.ir.exception.UserManagementException;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "change-password", layout = MainLayout.class)
public class ChangePasswordView extends VerticalLayout {

    private final UsersService usersService;

    public ChangePasswordView(UsersService usersService) {

        this.usersService = usersService;

        PasswordField prePassword = new PasswordField("現在のパスワード");
        PasswordField newPassword = new PasswordField("新しいパスワード");
        PasswordField confirmPassword = new PasswordField("新しいパスワード（確認）");

        Button changeButton = new Button("変更");

        changeButton.addClickListener(e -> {
            try {

                usersService.updateLoginUserPassword(
                        prePassword.getValue(),
                        newPassword.getValue(),
                        confirmPassword.getValue()
                );

                Notification.show("パスワードを変更しました");

                prePassword.clear();
                newPassword.clear();
                confirmPassword.clear();

            } catch (UserManagementException ex) {
                Notification.show(ex.getMessage());
            }
        });

        add(prePassword, newPassword, confirmPassword, changeButton);
    }
}
