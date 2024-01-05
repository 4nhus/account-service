package account.security;

import account.user.AccountUser;
import account.user.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SecurityService {
    private final SecurityLogRepository securityLogRepository;
    private final IncorrectPasswordRecordRepository incorrectPasswordRecordRepository;
    private final UserRepository userRepository;

    public SecurityService(SecurityLogRepository repository, IncorrectPasswordRecordRepository incorrectPasswordRecordRepository, UserRepository userRepository) {
        this.securityLogRepository = repository;
        this.incorrectPasswordRecordRepository = incorrectPasswordRecordRepository;
        this.userRepository = userRepository;
    }

    public List<SecurityLog> getSecurityLogs() {
        return securityLogRepository.findAll(Sort.by("id"));
    }

    @EventListener(SecurityEvent.class)
    public void logSecurityEvent(SecurityEvent event) {
        securityLogRepository.save(event.generateLog());

        if (event.getAction() == SecurityEvent.Action.LOGIN_FAILED) {
            logIncorrectPasswordAttempt(event);
        }
    }

    public void logIncorrectPasswordAttempt(SecurityEvent event) {
        Optional<AccountUser> user = userRepository.findUserByEmailIgnoreCase(event.getSubject());
        if (user.isPresent()) {
            IncorrectPasswordRecord record = incorrectPasswordRecordRepository.findByUsernameIgnoreCase(user.get().getEmail());

            if (record == null) {
                record = new IncorrectPasswordRecord(user.get().getEmail(), 1);
            } else {
                record.setCount(record.getCount() + 1);

                if (record.getCount() == 5) {
                    securityLogRepository.save(new SecurityEvent(this, LocalDate.now(), SecurityEvent.Action.BRUTE_FORCE, event.getSubject(), event.getPath(), event.getPath()).generateLog());
                    lockOrUnlockUser(user.get(), true, event.getSubject(), event.getPath());
                }
            }

            incorrectPasswordRecordRepository.save(record);
        }
    }

    @EventListener(AuthenticationSuccessEvent.class)
    public void resetIncorrectPasswordAttempts(AuthenticationSuccessEvent event) {
        AccountUser user = userRepository.findUserByEmailIgnoreCase(event.getAuthentication().getName()).get();
        IncorrectPasswordRecord record = incorrectPasswordRecordRepository.findByUsernameIgnoreCase(user.getEmail());

        if (record != null) {
            record.setCount(0);
            incorrectPasswordRecordRepository.save(record);
        }
    }

    public void lockOrUnlockUser(AccountUser user, boolean locked, String auth, String path) {
        SecurityEvent.Action action = locked ? SecurityEvent.Action.LOCK_USER : SecurityEvent.Action.UNLOCK_USER;
        user.setLocked(locked);
        userRepository.save(user);

        logSecurityEvent(new SecurityEvent(this, LocalDate.now(), action, auth, (locked ? "Lock" : "Unlock") + " user " + user.getEmail(), path));
    }
}
