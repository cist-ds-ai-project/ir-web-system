package jp.ac.ir.presentation.usermanagement;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import jp.ac.ir.service.management.UserManagementService;

import java.util.List;

// ユーザー管理画面のTop画面
@PageTitle("UserManagementTop")
@Route(value = "/user_management", layout = MainLayout.class)
@RolesAllowed({"admin"})
public class
UserManagementTopView extends VerticalLayout implements BreadcrumbProvider {
    private final UserManagementService userManagementService;
    private final UserManagementButtons userManagementButtons;
    private final UsersDataGrid usersDataGrid;


    public UserManagementTopView(UserManagementService userManagementService) {
        add(new H1("ユーザー管理画面"));
        add(new Paragraph("ユーザーの追加・情報の変更・無効化など管理が行えます。ログインIDは学籍番号と対応しています。"));
        this.userManagementService = userManagementService;
        userManagementButtons = new UserManagementButtons();
        usersDataGrid = new UsersDataGrid(this.userManagementService, UsersDataGrid.SelectionMode.SINGLE);
        add(userManagementButtons, usersDataGrid);
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        return List.of(
                BreadcrumbItem.of("ユーザー管理", UserManagementTopView.class)
        );
    }
}
