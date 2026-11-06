package jp.ac.ir.presentation.permission;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.model.entity.permission.Authority;
import jp.ac.ir.model.entity.permission.Role;
import jp.ac.ir.service.permission.PermissionService;
import jp.ac.ir.presentation.MainLayout;

import java.util.HashSet;
import java.util.Set;


@Route(value = "permission-manage", layout = MainLayout.class)
@RolesAllowed({"admin", "teacher", "student", "ir_committee"})
public class PermissionManageView extends VerticalLayout implements BeforeEnterObserver {

    private final PermissionService permissionService;

    private ComboBox<Role> roleComboBox = new ComboBox<>("対象ロールを選択");
    private Grid<Authority> grid = new Grid<>(Authority.class, false);
    private Set<Long> currentAllowedIds = new HashSet<>();

    public PermissionManageView(PermissionService permissionService) {
        this.permissionService = permissionService;

        add(new H2("ロール別権限管理設定"));

        // 1. ロール選択 ComboBox
        roleComboBox.setItems(permissionService.getAllRoles());
        roleComboBox.setItemLabelGenerator(Role::roleName);

        roleComboBox.addValueChangeListener(event -> refreshGrid(event.getValue()));
        add(roleComboBox);

        // 2. 権限一覧 Grid
        grid.addColumn(Authority::authority_name).setHeader("機能権限名").setAutoWidth(true);

        grid.addComponentColumn(auth -> {
            Checkbox cb = new Checkbox();
            cb.setValue(currentAllowedIds.contains(auth.id()));
            cb.setEnabled(roleComboBox.getValue() != null);

            cb.addValueChangeListener(event -> {
                Role selectedRole = roleComboBox.getValue();
                if (selectedRole != null) {
                    permissionService.updateRolePermission(
                            selectedRole.id(),
                            auth.id(),
                            event.getValue()
                    );
                    Notification.show(selectedRole.roleName() + " の権限を更新しました");
                }
            });
            return cb;
        }).setHeader("許可フラグ");

        add(grid);
        setSizeFull();
    }

    private void refreshGrid(Role selectedRole) {
        if (selectedRole == null) {
            currentAllowedIds = new HashSet<>();
        } else {
            currentAllowedIds = permissionService.getAllowedAuthorityIdsByRoleId(selectedRole.id());
        }
        grid.setItems(permissionService.getAllAuthorities());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // ログイン中のユーザー情報を取得
        jp.ac.ir.model.dto.permission.UserContext user = permissionService.getCurrentUser();

        if (user == null || !"admin".equals(user.roleName())) {
            Notification.show("管理者権限が必要です");
//            event.forwardTo("");
        }
    }
}