package jp.ac.ir.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@Configuration
public class PasswordEncoderConfiguration {

    // SecurityConfiguration に記述すると相互参照が発生したのでとりあえずクラスを分けた
    @Bean
    public PasswordEncoder passwordEncoder() {
        String idForEncode = "bcrypt";
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(idForEncode,
                Map.of(idForEncode, new BCryptPasswordEncoder()));
        return passwordEncoder;
    }

}
