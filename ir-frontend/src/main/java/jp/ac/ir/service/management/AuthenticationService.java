package jp.ac.ir.service.management;

import jp.ac.ir.repository.AuthenticationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {
    private final AuthenticationRepository authenticationRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(AuthenticationRepository authenticationRepository, PasswordEncoder passwordEncoder) {
        this.authenticationRepository = authenticationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> authenticate(String loginId, String password) {
        // userOpを空のOptionalで初期化
        Optional<User> userOp = Optional.empty();

        // DBと接続して情報を取ってくる処理。AuthenticationRepositoryで実際の処理をかく
        // loginIdからパスワードを取得
        Optional<Password> passwordOp = authenticationRepository.getPassword(loginId);
        System.out.println("認証処理");

        // そもそもloginIdが存在しなければ空のOptionalを返す
        if(passwordOp.isEmpty()){
            return userOp;
        }

        // ハッシュ化したパスワードが一致していればユーザ情報を取得
        if (passwordEncoder.matches(password, passwordOp.get().value())) {
            userOp = authenticationRepository.getUserInformation(loginId);
        }
        return userOp;
    }
}
