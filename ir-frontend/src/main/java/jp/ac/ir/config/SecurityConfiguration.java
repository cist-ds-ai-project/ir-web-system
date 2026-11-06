package jp.ac.ir.config;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import jp.ac.ir.presentation.view.LoginView;
import jp.ac.ir.service.management.AuthenticationService;
import jp.ac.ir.service.management.RoleService;
import jp.ac.ir.service.management.UserDetailsAuthenticationProvider;
import jp.ac.ir.service.management.UserManagementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration{

    private final AuthenticationService authenticationService;
    private final RoleService roleService;

    public SecurityConfiguration(AuthenticationService authenticationService, RoleService roleService) {
        this.authenticationService = authenticationService;
        this.roleService = roleService;
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        //Configure Vaadin's security using VaadinSecurityConfigurer
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
        });
        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new UserDetailsAuthenticationProvider(authenticationService, roleService);
    }

}