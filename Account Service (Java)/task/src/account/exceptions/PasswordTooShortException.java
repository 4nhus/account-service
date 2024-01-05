package account.exceptions;

import lombok.Getter;

@Getter
public class PasswordTooShortException extends RuntimeException {
    private final String path;

    public PasswordTooShortException(String path) {
        this.path = path;
    }
}
