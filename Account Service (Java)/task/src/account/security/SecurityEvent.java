package account.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

@Getter
@Setter
public class SecurityEvent extends ApplicationEvent {
    private LocalDate date;
    private Action action;
    private String subject;
    private String object;
    private String path;

    public SecurityEvent(Object source, LocalDate date, Action action, String subject, String object, String path) {
        super(source);
        this.date = date;
        this.action = action;
        this.subject = subject;
        this.object = object;
        this.path = path;
    }

    public SecurityLog generateLog() {
        return new SecurityLog(date, action.name(), subject, object, path);
    }

    public enum Action {
        CREATE_USER, CHANGE_PASSWORD, ACCESS_DENIED, LOGIN_FAILED, GRANT_ROLE, REMOVE_ROLE, LOCK_USER, UNLOCK_USER, DELETE_USER, BRUTE_FORCE
    }
}
