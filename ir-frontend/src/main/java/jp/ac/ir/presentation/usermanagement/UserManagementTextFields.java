package jp.ac.ir.presentation.usermanagement;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import jp.ac.ir.service.management.Role;
import jp.ac.ir.service.management.RoleService;
import jp.ac.ir.service.management.UsersData;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserManagementTextFields extends VerticalLayout {
    private CheckboxGroup<String> rolesCheckboxGroup;
    private TextField loginIDTextField;
    private TextField userNameTextField;
    private PasswordField userPasswordTextField;
    private final RoleService roleService;
    private final Map<String, Integer> allRolesMap;

    // 通常のコンストラクタ
    public UserManagementTextFields(RoleService roleService) {
        this.roleService = roleService;
        allRolesMap = roleService.getAllRolesMap();
        initializeTextField();
        initializeCheckBox();
        addComponentsToLayout();
    }

    // ユーザ情報を可視化する際のコンストラクタ
    public UserManagementTextFields(RoleService roleService, UsersData targetUser) {
        this.roleService = roleService;
        allRolesMap = roleService.getAllRolesMap();
        initializeTextField();
        initializeCheckBox();
        addUserInfomation(targetUser);
        addComponentsToLayout();
    }


    // テキストフィールドの初期化
    private void initializeTextField() {
        loginIDTextField = new TextField("ログインID");
        userNameTextField = new TextField("ユーザーネーム");
        userPasswordTextField = new PasswordField("パスワード");
    }

    // チェックボックスの初期化
    private void initializeCheckBox() {
        rolesCheckboxGroup = new CheckboxGroup<>();
        rolesCheckboxGroup.setLabel("権限");
        // ロールIDも保持できるか

        rolesCheckboxGroup.setItems(roleService.getRoles());
        add(rolesCheckboxGroup);
    }

    // 各テキストフィールドに現在の情報を表示
    private void addUserInfomation(UsersData targetUser){
        loginIDTextField.setLabel(loginIDTextField.getLabel() + " (現在：" + targetUser.login_id() + ")");
        userNameTextField.setLabel(userNameTextField.getLabel() + " (現在：" + targetUser.user_name() + ")");
        userPasswordTextField.setLabel(userPasswordTextField.getLabel() + " (現在の情報は表示できません)");
        StringBuilder roleLabel = new StringBuilder(rolesCheckboxGroup.getLabel());
        roleLabel.append(" (現在：");
        boolean isFirst = true;
        for(Role role : roleService.getRoleList(targetUser.id())){
            if(!isFirst) roleLabel.append(", ");
            roleLabel.append(role.display_name());
            isFirst = false;
        }
        roleLabel.append(")");
        rolesCheckboxGroup.setLabel(roleLabel.toString());
    }

    // 各種コンポーネントの追加
    private void addComponentsToLayout() {
        FormLayout formLayout = new FormLayout(loginIDTextField, userNameTextField, userPasswordTextField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        add(formLayout);
        add(rolesCheckboxGroup);
    }

    // 各コンポーネントから値を取得するメソッド
    public String getLoginID() {
        return loginIDTextField.getValue();
    }

    public String getUserName() {
        return userNameTextField.getValue();
    }

    public String getUserPassword() {
        return userPasswordTextField.getValue();
    }

    public Set<String> getRoles() {
        return rolesCheckboxGroup.getSelectedItems();
    }
    public Set<Integer> getRoleIds(){
        Set<Integer> selectedRoleId = new HashSet<>();
        for(String displayName : rolesCheckboxGroup.getSelectedItems()){
            selectedRoleId.add(allRolesMap.get(displayName));
        }
        return selectedRoleId;
    }
}
