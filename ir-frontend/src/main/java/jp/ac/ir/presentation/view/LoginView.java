package jp.ac.ir.presentation.view;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;


import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@AnonymousAllowed
    @PageTitle("login")
    @Route(value = "login")

    public class LoginView extends VerticalLayout implements BeforeEnterObserver {
        private final LoginForm login;
        private final LoginI18n i18n = LoginI18n.createDefault();

        public LoginView() {




            addClassName("login-view");
            setSizeFull();

            setJustifyContentMode(JustifyContentMode.CENTER);
            setAlignItems(Alignment.CENTER);

            // 各種メッセージに日本語を設定
            LoginI18n.Form i18nForm = i18n.getForm();

            i18nForm.setTitle("ログイン");
            i18nForm.setUsername("ログインID");
            i18nForm.setPassword("パスワード");
            i18nForm.setSubmit("ログイン");
            i18nForm.setForgotPassword("パスワードを忘れた方はこちら");
            i18n.setForm(i18nForm);

            LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();

            i18nErrorMessage.setTitle("ログインIDまたはパスワードが不正です");
            i18nErrorMessage.setUsername("ログインIDを入力してください");
            i18nErrorMessage.setPassword("パスワードを入力してください");
            i18nErrorMessage.setMessage("ユーザー名とパスワードが正しいことを確認して、再試行してください");

            login = new LoginForm(i18n);
            // パスワードを忘れた人向けの表示を削除
            login.setForgotPasswordButtonVisible(false);
            login.setAction("login");//kokokaieta

            // タイトルとフォームを画面に出力
            add(new H1("CIST IR-Web"), login);
            //add(login);
        }

        @Override
        public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
            if(beforeEnterEvent.getLocation()
                    .getQueryParameters()
                    .getParameters()
                    .containsKey("error")) {
                login.setError(true);
            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !(auth instanceof AnonymousAuthenticationToken)) {
                beforeEnterEvent.forwardTo("top");
            }

        }
    }
