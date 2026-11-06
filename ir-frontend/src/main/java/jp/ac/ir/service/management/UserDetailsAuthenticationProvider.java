package jp.ac.ir.service.management;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UserDetailsAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {


    private final AuthenticationService authenticationService;
    private final RoleService roleService;

    public UserDetailsAuthenticationProvider(AuthenticationService authenticationService, RoleService roleService) {
        this.authenticationService = authenticationService;
        this.roleService = roleService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    }

    @Override
    protected UserDetails retrieveUser(String loginId, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {

        String password = (String) authentication.getCredentials(); // authenticationからpasswordを取得

        Optional<User> loginUserOp = authenticationService.authenticate(loginId, password);
        System.out.print("認証結果: ");
        if (loginUserOp.isEmpty()) {
            System.out.println("認証に失敗しました. login_id=" + loginId);
            // 上手くいかなかったらかえる
            throw new BadCredentialsException("認証に失敗しました。");
        }
        System.out.println("認証に成功しました. login_id=" + loginId);
        System.out.println(loginUserOp.get());

        User user = loginUserOp.get();
        Set<String> roles = new HashSet<>();

        for(Role role : roleService.getRoleList(user.id())){
            roles.add("ROLE_" + role.name());
        }

        LoginUser loginUser = new LoginUser(user, roles);
        System.out.println("isAdmin : " + loginUser.isAdmin());
        System.out.println("isTeacher : " + loginUser.isTeacher());
        System.out.println("isStudent : " + loginUser.isStudent());
        System.out.println("isCommission : " + loginUser.isCommission());

        return loginUser;
    }
}