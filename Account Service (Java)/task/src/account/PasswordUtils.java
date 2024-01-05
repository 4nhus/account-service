package account;

import java.util.Arrays;

public class PasswordUtils {
    public static boolean isPasswordTooShort(String password) {
        return Arrays.stream(password.split("")).filter(s -> !s.isBlank()).count() < 12;
    }

    public static boolean isBreachedPassword(String password) {
        return password.matches("PasswordFor((January)|(February)|(March)|(April)|(May)|(June)|(July)|(August)|(September)|(October)|(November)|(December))");
    }
}
