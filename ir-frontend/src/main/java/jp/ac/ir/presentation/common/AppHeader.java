//package jp.ac.ir.presentation.common;
//
//import com.vaadin.flow.component.Component;
//import com.vaadin.flow.component.html.Header;
//import com.vaadin.flow.component.html.ListItem;
//import com.vaadin.flow.component.html.Nav;
//import com.vaadin.flow.component.html.Span;
//import com.vaadin.flow.component.html.UnorderedList;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.menubar.MenuBar;
//import com.vaadin.flow.component.orderedlayout.FlexComponent;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.UI;
//import com.vaadin.flow.router.RouterLink;
//import java.util.ArrayList;
//import java.util.List;
////import jp.ac.ir.presentation.sample.SampleGpaView;
//import jp.ac.ir.presentation.usermanagement.UserManagementTopView;
//import jp.ac.ir.presentation.view.ChangePasswordView;
//import jp.ac.ir.presentation.view.GradeInfoView;
//import jp.ac.ir.presentation.view.QuestionnaireView;
//import jp.ac.ir.presentation.scholarship.ScholarshipView;
//import jp.ac.ir.presentation.view.TeacherView;
//import jp.ac.ir.presentation.view.TopView;
//import jp.ac.ir.service.management.LoginUser;
//import jp.ac.ir.service.management.SecurityService;
//
//public class AppHeader extends Header {
//
//    public AppHeader(SecurityService securityService) {
//        addClassName("app-header");
//        add(createTopRow(securityService), createNavigation(securityService.getLoginUser()));
//    }
//
//    private Component createTopRow(SecurityService securityService) {
//        HorizontalLayout topRow = new HorizontalLayout();
//        topRow.addClassName("app-header__top-row");
//        topRow.setWidthFull();
//        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
//        topRow.setPadding(false);
//        topRow.setSpacing(false);
//
//        RouterLink titleLink = new RouterLink();
//        titleLink.setRoute(TopView.class);
//        titleLink.addClassName("app-header__brand");
//        titleLink.add(new Span("IR"));
//
//        HorizontalLayout actions = new HorizontalLayout();
//        actions.addClassName("app-header__actions");
//        actions.setAlignItems(FlexComponent.Alignment.CENTER);
//        actions.setSpacing(true);
//        actions.setPadding(false);
//
//        Span username = new Span(resolveDisplayName(securityService.getLoginUser()));
//        username.addClassName("app-header__username");
//
//        MenuBar userMenu = createUserMenu(securityService);
//
//        actions.add(username, userMenu);
//        topRow.add(titleLink, actions);
//        topRow.expand(titleLink);
//        return topRow;
//    }
//
//    private Component createNavigation(LoginUser loginUser) {
//        Nav nav = new Nav();
//        nav.addClassName("app-header__nav");
//
//        UnorderedList list = new UnorderedList();
//        list.addClassName("app-header__menu");
//        nav.add(list);
//
//        for (MenuItem item : buildMenuItems(loginUser)) {
//            list.add(createMenuLink(item));
//        }
//        return nav;
//    }
//
//    private ListItem createMenuLink(MenuItem item) {
//        ListItem wrapper = new ListItem();
//        wrapper.addClassName("app-header__menu-item");
//
//        RouterLink link = new RouterLink();
//        link.setRoute(item.view());
//        link.addClassName("app-header__menu-link");
//        link.add(item.icon().create(), new Span(item.label()));
//
//        wrapper.add(link);
//        return wrapper;
//    }
//
//    private List<MenuItem> buildMenuItems(LoginUser loginUser) {
//        List<MenuItem> items = new ArrayList<>();
//        items.add(new MenuItem("Top", VaadinIcon.HOME, TopView.class));
//        items.add(new MenuItem("成績情報", VaadinIcon.BOOK, GradeInfoView.class));
////        items.add(new MenuItem("GPA統計", VaadinIcon.BAR_CHART, SampleGpaView.class));
//        items.add(new MenuItem("アンケート", VaadinIcon.CLIPBOARD_TEXT, QuestionnaireView.class));
//        items.add(new MenuItem("奨学金", VaadinIcon.MONEY, ScholarshipView.class));
//        items.add(new MenuItem("Teacher", VaadinIcon.USER, TeacherView.class));
//
//        if (loginUser != null && loginUser.isAdmin()) {
//            items.add(new MenuItem("ユーザー管理", VaadinIcon.USERS, UserManagementTopView.class));
//        }
//        return items;
//    }
//
//    private String resolveDisplayName(LoginUser loginUser) {
//        if (loginUser == null || loginUser.getUsername() == null || loginUser.getUsername().isBlank()) {
//            return "User";
//        }
//        return loginUser.getUsername();
//    }
//
//    private MenuBar createUserMenu(SecurityService securityService) {
//        MenuBar menuBar = new MenuBar();
//        menuBar.addClassName("app-header__user-menu");
//
//        var rootItem = menuBar.addItem(VaadinIcon.MENU.create());
//        rootItem.getSubMenu().addItem("パスワード変更", event -> UI.getCurrent().navigate(ChangePasswordView.class));
//        rootItem.getSubMenu().addItem("Logout", event -> securityService.logout());
//        return menuBar;
//    }
//
//    private record MenuItem(String label, VaadinIcon icon, Class<? extends Component> view) {
//    }
//}
