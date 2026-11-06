package jp.ac.ir.service.management;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginUserTest {
    private final User account = new User(1L, "test", "test-person", "Test", true);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void teacherWithAdminRoleHasNoEffectiveAdminAccess() {
        Set<String> assignedRoles = new HashSet<>(Set.of("ROLE_teacher", "ROLE_admin", "ROLE_ir_committee"));
        LoginUser user = new LoginUser(account, assignedRoles);
        assertTrue(user.isTeacher());
        assertTrue(user.isCommission());
        assertFalse(user.isAdmin());
        assertTrue(assignedRoles.contains("ROLE_admin"));
        assertFalse(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin")));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        assertFalse(new SecurityService().hasRole("admin"));
        assertTrue(new SecurityService().hasRole("teacher"));
    }

    @Test
    void teacherOnlyIsNotAdmin() {
        LoginUser user = new LoginUser(account, Set.of("ROLE_teacher"));
        assertTrue(user.isTeacher());
        assertFalse(user.isAdmin());
    }

    @Test
    void adminOnlyRetainsAdminAccess() {
        LoginUser user = new LoginUser(account, Set.of("ROLE_admin"));
        assertTrue(user.isAdmin());
        assertFalse(user.isTeacher());
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin")));
    }
}
