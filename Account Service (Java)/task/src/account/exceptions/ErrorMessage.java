package account.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ErrorMessage {
    public ErrorMessage(LocalDateTime timestamp, HttpStatus status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status.value();
        this.error = error;
        this.message = message;
        this.path = path;
    }

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
