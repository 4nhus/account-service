package account.exceptions;

import lombok.Getter;

@Getter
public class BreachedPasswordException extends RuntimeException {
    private final String path;

    public BreachedPasswordException(String path) {
        this.path = path;
    }
}
