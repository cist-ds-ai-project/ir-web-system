package jp.ac.ir.presentation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import jakarta.annotation.security.PermitAll;
import jp.ac.ir.presentation.gpa.GpaView;
import jp.ac.ir.presentation.personal.PersonalView;
import jp.ac.ir.presentation.view.TopView;
import jp.ac.ir.presentation.chat.ChatView;
import jp.ac.ir.presentation.lecture.LectureSelectView;
import jp.ac.ir.presentation.navigation.BreadcrumbItem;
import jp.ac.ir.presentation.navigation.BreadcrumbProvider;
import jp.ac.ir.presentation.scholarship.ScholarshipView;
import jp.ac.ir.presentation.usermanagement.UserManagementTopView;
import jp.ac.ir.presentation.view.ChangePasswordView;
import jp.ac.ir.service.management.SecurityService;

import java.util.List;

@PermitAll
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final HorizontalLayout breadcrumbs = new HorizontalLayout();
    private final SecurityService securityService;

    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, Component icon, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            link.addClassNames(
                    Display.FLEX,
                    Gap.XSMALL,
                    Height.MEDIUM,
                    AlignItems.CENTER,
                    Padding.Horizontal.SMALL,
                    TextColor.BODY
            );
            link.setRoute(view);

            Span text = new Span(menuTitle);
            text.addClassNames(FontWeight.MEDIUM, FontSize.MEDIUM, Whitespace.NOWRAP);

            if (icon != null) {
                link.add(icon);
            }
            link.add(text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }
    }

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        addToNavbar(createHeaderContent());
        setDrawerOpened(false);
    }

    private Component createHeaderContent() {
        Header header = new Header();
        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.COLUMN, Width.FULL);

        Div layout = new Div();
        layout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.Horizontal.LARGE);
        layout.getStyle().set("gap", "12px");

        RouterLink appHome = new RouterLink();
        appHome.setRoute(TopView.class);
        appHome.addClassNames(Display.FLEX, AlignItems.CENTER, Margin.Vertical.MEDIUM, TextColor.BODY);
        appHome.getStyle().set("text-decoration", "none");

        Span appTitle = new Span("IR-WEB");
        appTitle.addClassNames(FontSize.LARGE, FontWeight.BOLD);
        appTitle.getStyle().set("font-size", "2.25rem");
        appHome.add(appTitle);

        breadcrumbs.setSpacing(false);
        breadcrumbs.setPadding(false);
        breadcrumbs.setAlignItems(FlexComponent.Alignment.CENTER);
        breadcrumbs.getStyle().set("gap", "8px");

        HorizontalLayout accountActions = new HorizontalLayout();
        accountActions.setSpacing(true);
        accountActions.setAlignItems(FlexComponent.Alignment.CENTER);
        accountActions.getStyle().set("margin-left", "auto");

        Button changePasswordButton = new Button(
                "パスワード変更",
                event -> UI.getCurrent().navigate(ChangePasswordView.class)
        );
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button logoutButton = new Button(
                "ログアウト",
                event -> securityService.logout()
        );
        logoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        accountActions.add(changePasswordButton, logoutButton);

        layout.add(appHome, breadcrumbs, accountActions);

        Nav nav = new Nav();
        nav.addClassNames(Display.FLEX, Overflow.AUTO, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL);
        nav.getStyle().set("padding-top", "0");
        nav.getStyle().set("padding-bottom", "0");

        UnorderedList list = new UnorderedList();
        list.addClassNames(Display.FLEX, Gap.SMALL, ListStyleType.NONE, Margin.NONE, Padding.NONE);
        nav.add(list);

        for (Component menuItem : createMenuItems()) {
            list.add(menuItem);
        }

        header.add(layout, nav);
        return header;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        breadcrumbs.removeAll();

        List<HasElement> activeChain = event.getActiveChain();
        if (activeChain.isEmpty()) {
            return;
        }

        BreadcrumbProvider provider = activeChain.stream()
                .filter(BreadcrumbProvider.class::isInstance)
                .map(BreadcrumbProvider.class::cast)
                .findFirst()
                .orElse(null);
        if (provider == null) {
            return;
        }

        List<BreadcrumbItem> items = provider.getBreadcrumbItems();
        for (int i = 0; i < items.size(); i++) {
            breadcrumbs.add(createSeparator());

            BreadcrumbItem item = items.get(i);
            if (i == items.size() - 1) {
                Span current = new Span(item.label());
                current.getStyle().set("font-weight", "600");
                breadcrumbs.add(current);
            } else {
                breadcrumbs.add(createBreadcrumbLink(item));
            }
        }
    }

    private Span createSeparator() {
        Span separator = new Span("/");
        separator.getStyle().set("color", "var(--lumo-secondary-text-color)");
        return separator;
    }

    private RouterLink createBreadcrumbLink(BreadcrumbItem item) {
        RouterLink link = new RouterLink();
        link.setText(item.label());
        link.getStyle().set("text-decoration", "none");

        RouteParameters routeParameters = item.routeParameters();
        if (routeParameters == null) {
            link.setRoute(item.navigationTarget());
        } else {
            link.setRoute(item.navigationTarget(), routeParameters);
        }
        if (item.queryParameters() != null) {
            link.setQueryParameters(item.queryParameters());
        }

        return link;
    }

    private Component[] createMenuItems() {
        var items = new java.util.ArrayList<Component>(List.of(
                new MenuItemInfo("TOP", null, TopView.class),
                new MenuItemInfo("成績情報", null, LectureSelectView.class),
                new MenuItemInfo("GPA統計", null, GpaView.class),
                new MenuItemInfo("奨学金", null, ScholarshipView.class),
                new MenuItemInfo("AIチャット", null, ChatView.class)
        ));
        if (securityService.hasRole("admin")) {
            items.add(4, new MenuItemInfo("ユーザー管理", null, UserManagementTopView.class));
        }
        if (securityService.hasRole("student")) {
            items.add(new MenuItemInfo("個人成績", null, PersonalView.class));
        }
        return items.toArray(Component[]::new);
    }

}
