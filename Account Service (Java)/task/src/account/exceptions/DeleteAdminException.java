package account.exceptions;

import lombok.Getter;

@Getter
public class DeleteAdminException extends RuntimeException {
    private final String path;

    public DeleteAdminException(String path) {
        this.path = path;
    }
}