package jp.ac.ir.presentation.usermanagement;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.exception.UserManagementException;
import jp.ac.ir.service.management.UsersService;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.UploadButton;
import jp.ac.ir.presentation.component.notification.ErrorNotification;
import jp.ac.ir.presentation.component.notification.SuccessNotification;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;

import java.io.InputStream;
import java.util.List;

@PageTitle("UserBulkAdd")
@Route(value = "/user_management/bulk_add", layout = MainLayout.class)
@RolesAllowed({"admin"})
public class UserBulkAddView extends VerticalLayout implements BreadcrumbProvider {
    private Button cancelButton;
    private Upload upload;
    private final UsersService usersService;

    public UserBulkAddView(UsersService usersService) {
        this.usersService = usersService;
        initializeButton();
        initializeUploadButton();
        addComponents();
    }

    // ボタン
    private void initializeButton() {
        cancelButton = new Button("戻る", buttonClickEvent -> {
            UI.getCurrent().navigate("/user_management");
        });
    }

    // ファイルのアップロードボタン
    private void initializeUploadButton() {
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        upload = new Upload(buffer);
        upload.setDropAllowed(true);
        upload.setAutoUpload(false);

        // csv形式のファイルのみ受付
        upload.setAcceptedFileTypes("text/csv", ".csv");
        // 1度にアップロードできるファイルを1つに設定
        upload.setMaxFiles(1);

        Button uploadButton = new Button("追加");
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


        UploadButton i18n = new UploadButton();
        upload.setI18n(i18n);

        upload.setUploadButton(uploadButton);

        upload.addSucceededListener(event -> {
            String filename = event.getFileName();
            InputStream inputStream = buffer.getInputStream(filename);

            // ユーザ一括追加処理
            // 正常終了→成功メッセージ 異常終了→エラーメッセージ
            try {
                long inserted = usersService.addUsers(inputStream);
                new SuccessNotification(inserted + " 件のユーザ追加に成功");
            } catch (UserManagementException e) {
                if (e.getMessage().isEmpty()) new ErrorNotification("エラーが発生しました");
                else new ErrorNotification(e.getMessage());
            } catch (RuntimeException e) {
                e.printStackTrace();
                new ErrorNotification("エラーが発生しました");
            }
        });
    }

    // 各種コンポーネントの追加
    private void addComponents() {
        add(new H1("ユーザーの一括追加"));
        add(new Html("<div>ユーザーを一括で追加できます。追加したいユーザーの情報をcsv形式で用意してください。<br>csvは一列目：ログインID, 二列目：ユーザーネーム, 三列目：パスワード, 四列目以降：付与したい権限名（複数の場合は一列ずつ記入）の形式で作成してください。<br>また、パスワードは12文字以上で設定してください。</div>"));
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton);
        buttonLayout.getStyle().set("flex-wrap", "wrap");
        add(buttonLayout);
        add(upload);
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbItems() {
        return List.of(
                BreadcrumbItem.of("ユーザー管理", UserManagementTopView.class),
                BreadcrumbItem.of("ユーザーの一括追加", UserBulkAddView.class)
        );
    }
}