package jp.ac.ir.service.management;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LoginUser implements UserDetails {

    private User user;
    private Set<String> roles;
    private Collection<GrantedAuthority> authorities;

    public LoginUser(User user, Set<String> roles) {
        this.user = user;
        Set<String> effectiveRoles = new HashSet<>(roles);
        // 教員としてログインするアカウントには管理者権限を付与しない。
        if (effectiveRoles.contains("ROLE_teacher")) {
            effectiveRoles.remove("ROLE_admin");
        }
        this.roles = Set.copyOf(effectiveRoles);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities != null) {
            return authorities;
        }

        authorities = new ArrayList<>();

        for(String role : roles) {
            GrantedAuthority authority = new SimpleGrantedAuthority(role);
            authorities.add(authority);
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return user.name();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getPassword() {
        return "";
    }

    public long getAccountId(){
        return user.id();
    }

    public String getLoginId() {return user.login_id(); }
    
    public String getPersonId() {return user.person_id(); }

    public boolean isTeacher() { return roles.contains("ROLE_teacher"); }

    public boolean isStudent() { return roles.contains("ROLE_student"); }

    public boolean isCommission() { return roles.contains("ROLE_ir_committee"); }

    public boolean isAdmin() { return roles.contains("ROLE_admin"); }

}
