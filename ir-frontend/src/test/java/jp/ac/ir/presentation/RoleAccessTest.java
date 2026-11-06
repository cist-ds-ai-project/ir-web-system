package jp.ac.ir.presentation;

import jakarta.annotation.security.RolesAllowed;
import jp.ac.ir.presentation.personal.PersonalView;
import jp.ac.ir.presentation.personal.PersonalDetailView;
import jp.ac.ir.presentation.teacher.TeacherCourseView;
import jp.ac.ir.presentation.teacher.TeacherCourseDetailView;
import jp.ac.ir.presentation.view.TopView;
import jp.ac.ir.service.management.SecurityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class RoleAccessTest {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {"student", "teacher", "admin", "ir_committee"})
    void homeShowsOnlyTheAppropriateRoleLinks(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "unused",
                        AuthorityUtils.createAuthorityList("ROLE_" + role)));
        String home = new TopView(new SecurityService()).getElement().getTextRecursively();
        assertEquals(role.equals("student"), home.contains("個人成績"));
        assertEquals(role.equals("teacher"), home.contains("担当した科目の詳細情報"));
        assertEquals(role.equals("admin"), home.contains("ユーザー管理"));
        assertFalse(home.contains("大学情報"));
        assertTrue(home.contains("成績情報"));
        assertTrue(home.contains("GPA統計"));
    }

    @Test
    void listAndDetailRoutesRequireTheCorrectRole() {
        for (Class<?> view : new Class<?>[]{PersonalView.class, PersonalDetailView.class}) {
            assertArrayEquals(new String[]{"student"}, view.getAnnotation(RolesAllowed.class).value());
        }
        for (Class<?> view : new Class<?>[]{TeacherCourseView.class, TeacherCourseDetailView.class}) {
            assertArrayEquals(new String[]{"teacher"}, view.getAnnotation(RolesAllowed.class).value());
        }
    }

    @Test
    void missingAuthenticationGrantsNeitherRole() {
        SecurityService service = new SecurityService();
        assertFalse(service.hasRole("student"));
        assertFalse(service.hasRole("teacher"));
    }
}
