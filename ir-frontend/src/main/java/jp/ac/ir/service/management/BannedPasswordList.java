package jp.ac.ir.service.management;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * クラスパスの banned-passwords.txt からよくあるパスワードリストを読み込み、
 * 照合用に保持する。ファイルが存在しないまたは空の場合は空の Set として扱う。
 * SecLists 等のリストを banned-passwords.txt として配置して利用する。
 */
@Component
public class BannedPasswordList {

    private final Set<String> bannedSet;

    public BannedPasswordList() {
        this.bannedSet = loadFromClasspath();
    }

    private static Set<String> loadFromClasspath() {
        try (InputStream is = BannedPasswordList.class.getClassLoader()
                .getResourceAsStream("banned-passwords.txt")) {
            if (is == null) {
                return Collections.emptySet();
            }
            Set<String> set = new HashSet<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        set.add(trimmed.toLowerCase());
                    }
                }
            }
            return Collections.unmodifiableSet(set);
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }

    /**
     * 指定したパスワードが禁止リストに含まれるか判定する。
     * 照合時は小文字に正規化して比較する。
     */
    public boolean isBanned(String password) {
        if (password == null || bannedSet.isEmpty()) {
            return false;
        }
        return bannedSet.contains(password.toLowerCase());
    }
}
