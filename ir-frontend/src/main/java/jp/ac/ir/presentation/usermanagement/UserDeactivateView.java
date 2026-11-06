package jp.ac.ir.presentation.usermanagement;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.exception.UserManagementException;
import jp.ac.ir.service.management.UserManagementService;
import jp.ac.ir.service.management.UsersData;
import jp.ac.ir.service.management.UsersService;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.component.notification.ErrorNotification;
import jp.ac.ir.presentation.component.notification.SuccessNotification;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;

import java.util.List;
import java.util.Set;

@PageTitle("UserDeactivate")
@Route(value = "/user_management/deactivate", layout = MainLayout.class)
@RolesAllowed({"admin"})
public class UserDeactivateView extends VerticalLayout implements BreadcrumbProvider {
    private final UserManagementService userManagementService;
    private final UsersService usersService;
    private Button deactivateAccount;
    private Button cancelButton;
    private final UsersDataGrid usersDataGrid;

    // コンストラクタ
    public UserDeactivateView(UserManagementService userManagementService, UsersService usersService) {
        // 文字列がセッションに渡されていたら成功メッセージを出力→セッションの文字列をnullに戻す
        if(UI.getCurrent().getSession().getAttribute(String.class) != null) {
            new SuccessNotification(UI.getCurrent().getSession().getAttribute(String.class));
            UI.getCurrent().getSession().setAttribute(String.class, null);
        }
        this.userManagementService = userManagementService;
        this.usersService = usersService;
        initializeButton();
        usersDataGrid = new UsersDataGrid(this.userManagementService, UsersDataGrid.SelectionMode.MULTI);
        addComponents();
    }


    // ボタンの初期設定
    private void initializeButton() {
        deactivateAccount = new Button("無効化", new Icon(VaadinIcon.BAN), buttonClickEvent -> {
            // 選択されているユーザーの情報を取得
            Set<UsersData> selectedUsers = usersDataGrid.getGrid().getSelectedItems();

            // ユーザ無効化処理
            // 正常終了→成功メッセージ 異常終了→エラーメッセージ
            try {
                usersService.deactivateUsers(selectedUsers);
                // 成功メッセージをセッションに渡して画面をリロード
                UI.getCurrent().getSession().setAttribute(String.class, selectedUsers.size() + " 件のユーザの無効化に成功");
                UI.getCurrent().getPage().reload();
            } catch (UserManagementException e){
                if(e.getMessage().isEmpty()) new ErrorNotification("エラーが発生しました");
                else new ErrorNotification(e.getMessage());
            } catch (RuntimeException e){
                e.printStackTrace();
                new ErrorNotification("エラーが発生しました");
            }
        });
        deactivateAccount.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton = new Button("戻る", buttonClickEvent -> {
            UI.getCurrent().navigate("/user_management");
        });
    }

    // 各種コンポーネントの追加
    private void addComponents() {
        add(new H1("ユーザーの無効化"), new Paragraph("ユーザーのアクセス権限を無効化することができます。無効化したいユーザーを選んで無効化ボタンを押してください。"));
        add(cancelButton);
        add(usersDataGrid);
        add(deactivateAccount);
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        return List.of(
                BreadcrumbItem.of("ユーザー管理", UserManagementTopView.class),
                BreadcrumbItem.of("ユーザーの無効化", UserDeactivateView.class)
        );
    }
}
