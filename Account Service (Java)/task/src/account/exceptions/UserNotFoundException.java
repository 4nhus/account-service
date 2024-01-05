package account.exceptions;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final String path;

    public UserNotFoundException(String path) {
        this.path = path;
    }
}
