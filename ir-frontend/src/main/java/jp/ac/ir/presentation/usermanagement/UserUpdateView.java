package jp.ac.ir.presentation.usermanagement;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.exception.UserManagementException;
import jp.ac.ir.service.management.RoleService;
import jp.ac.ir.service.management.User;
import jp.ac.ir.service.management.UsersData;
import jp.ac.ir.service.management.UsersService;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.component.notification.ErrorNotification;
import jp.ac.ir.presentation.component.notification.SuccessNotification;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;

import java.util.List;
import java.util.Set;

@PageTitle("UserUpdate")
@Route(value = "/user_management/updateView", layout = MainLayout.class)
@RolesAllowed({"admin" +
        ""})
public class UserUpdateView extends VerticalLayout implements BreadcrumbProvider {
    private final UsersService usersService;
    private Button updateAccount;
    private Button cancelButton;
    private final UsersData targetUser;
    private UserManagementTextFields userManagementTextFields;

    public UserUpdateView(UsersService usersService, RoleService roleService) {
        this.usersService = usersService;

        // 選択したユーザーの情報を取得
        this.targetUser = (UsersData) UI.getCurrent().getSession().getAttribute(UsersData.class);
        // 文字列がセッションに渡されていたら成功メッセージを出力→セッションの文字列をnullに戻す
        if(UI.getCurrent().getSession().getAttribute(String.class) != null) {
            new SuccessNotification(UI.getCurrent().getSession().getAttribute(String.class));
            UI.getCurrent().getSession().setAttribute(String.class, null);
        }
        userManagementTextFields = new UserManagementTextFields(roleService, targetUser);
        initializeButton();
        addComponents();

    }

    // ボタンの初期設定
    private void initializeButton() {
        updateAccount = new Button("変更", buttonClickEvent -> {
            // テキストフィールド・チェックボックス上の値をサービスに渡す
            String newLoginID = userManagementTextFields.getLoginID();
            String newUserName = userManagementTextFields.getUserName();
            String newPassword = userManagementTextFields.getUserPassword();
            Set<Integer> newRoleIds = userManagementTextFields.getRoleIds();

            User castedtargetUser = new User(targetUser.id(), targetUser.login_id(), targetUser.person_id(), targetUser.user_name(), targetUser.is_available());
            try {
                UsersData updatedUser = usersService.updateUser(castedtargetUser, newLoginID, newUserName, newPassword, newRoleIds);

                // 変更後のユーザ情報と成功メッセージをセッションに渡して画面をリロード
                UI.getCurrent().getSession().setAttribute(UsersData.class, updatedUser);
                UI.getCurrent().getSession().setAttribute(String.class, targetUser.user_name() + "さんの情報を変更しました");
                UI.getCurrent().getPage().reload();
            } catch (UserManagementException e) {
                if (e.getMessage().isEmpty()) new ErrorNotification("エラーが発生しました");
                else new ErrorNotification(e.getMessage());
            } catch (RuntimeException e) {
                e.printStackTrace();
                new ErrorNotification("エラーが発生しました");
            }
        });
        updateAccount.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton = new Button("戻る", buttonClickEvent -> {
            UI.getCurrent().navigate("/user_management");
        });
    }

    // 各種コンポーネントの追加
    private void addComponents() {
        add(new H1("ユーザーの情報変更"), new Paragraph("ユーザーの情報を変更することができます。変更したい情報のみ入力してください。\n入力があった情報のみ変更されます。また、パスワードは12文字以上で設定してください。"));
        add(cancelButton);
        add(userManagementTextFields);
        add(updateAccount);
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        return List.of(
                BreadcrumbItem.of("ユーザー管理", UserManagementTopView.class),
                BreadcrumbItem.of("ユーザー情報の変更", UserUpdateView.class)
        );
    }

}
