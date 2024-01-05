package account;

import account.exceptions.*;
import account.group.Group;
import account.payroll.Payroll;
import account.security.SecurityLog;
import account.security.SecurityService;
import account.user.AccountUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@RestController
public class AccountRestController {
    private final AccountService accountService;
    private final SecurityService securityService;

    public AccountRestController(AccountService accountService, SecurityService securityService) {
        this.accountService = accountService;
        this.securityService = securityService;
    }


    @PostMapping("api/auth/signup")
    public ResponseEntity<UserResponse> signupUser(@Valid @RequestBody AccountUser user, Authentication auth) {
        if (PasswordUtils.isPasswordTooShort(user.getPassword())) {
            throw new PasswordTooShortException("/api/auth/signup");
        }

        if (PasswordUtils.isBreachedPassword(user.getPassword())) {
            throw new BreachedPasswordException("/api/auth/signup");
        }

        user = accountService.addUser(user, auth == null ? null : auth.getName());

        return ResponseEntity.ok(formatUserAsResponse(user));
    }

    @PostMapping("api/auth/changepass")
    public ResponseEntity<UpdatedPasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication auth) {
        if (PasswordUtils.isPasswordTooShort(request.new_password)) {
            throw new PasswordTooShortException("/api/auth/changepass");
        }

        if (PasswordUtils.isBreachedPassword(request.new_password)) {
            throw new BreachedPasswordException("/api/auth/changepass");
        }

        accountService.changeUserPassword(auth.getName(), request.new_password);

        return ResponseEntity.ok(new UpdatedPasswordResponse(auth.getName(), "The password has been updated successfully"));
    }


    @PostMapping("api/acct/payments")
    public ResponseEntity<StatusResponse> uploadPayrolls(@RequestBody List<Payroll> payrolls) {
        accountService.addPayrolls(payrolls);

        return ResponseEntity.ok(new StatusResponse("Added successfully!"));
    }

    @PutMapping("api/acct/payments")
    public ResponseEntity<StatusResponse> updatePayroll(@RequestBody Payroll payroll) {
        accountService.updatePayroll(payroll);

        return ResponseEntity.ok(new StatusResponse("Updated successfully!"));
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ErrorMessage> handleInvalidPeriod() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "Period month is invalid!", "/api/acct/payments"));
    }


    @GetMapping("api/empl/payment")
    public ResponseEntity<Object> getPayrolls(@RequestParam(required = false, name = "period") String periodString, Authentication auth) {
        if (periodString == null) {
            return ResponseEntity.ok(accountService.getAllPayrollsForUser(auth.getName()).stream().map(this::formatPayrollAsResponse).toList());
        } else {
            String[] periodParts = periodString.split("-");
            int month = Integer.parseInt(periodParts[0]);
            int year = Integer.parseInt(periodParts[1]);

            if (month < 1 || month > 12) {
                throw new InvalidPeriodException();
            }

            return ResponseEntity.ok(formatPayrollAsResponse(accountService.getPayrollForUser(auth.getName(), LocalDate.of(year, month, 1)).orElse(null)));
        }
    }

    @GetMapping("api/admin/user/")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(accountService.getUsers().stream().map(this::formatUserAsResponse).toList());
    }

    @DeleteMapping("api/admin/user/{email}")
    public ResponseEntity<DeletedUserResponse> deleteUser(@PathVariable String email, Authentication auth) {
        if (accountService.getUserFromEmail(email) == null) {
            throw new UserNotFoundException("/api/admin/user/" + email);
        }

        accountService.deleteUser(email, auth.getName());

        return ResponseEntity.ok(new DeletedUserResponse(email, "Deleted successfully!"));

    }

    @PutMapping("api/admin/user/role")
    public ResponseEntity<UserResponse> changeRoles(@RequestBody ChangeRoleRequest request, Authentication auth) {
        if (!request.role.matches("(ADMINISTRATOR)|(ACCOUNTANT)|(USER)|(AUDITOR)")) {
            throw new RoleNotFoundException();
        }

        Group.Role role = switch (request.role) {
            case "ADMINISTRATOR" -> Group.Role.ADMINISTRATOR;
            case "ACCOUNTANT" -> Group.Role.ACCOUNTANT;
            case "AUDITOR" -> Group.Role.AUDITOR;
            default -> Group.Role.USER;
        };

        return ResponseEntity.ok(formatUserAsResponse(accountService.changeUserRole(request.user, role, request.operation, auth.getName())));
    }

    @PutMapping("api/admin/user/access")
    public ResponseEntity<StatusResponse> lockOrUnlockUser(@Valid @RequestBody LockOrUnlockUserRequest request, Authentication auth) {
        AccountUser user = accountService.getUserFromEmail(request.user);

        if (request.operation.equals("LOCK") && user.isAdmin()) {
            throw new LockAdminException();
        }

        securityService.lockOrUnlockUser(user, request.operation.equals("LOCK"), auth.getName(), "/api/admin/user/access");

        return ResponseEntity.ok(new StatusResponse("User " + request.user.toLowerCase() + " " + (request.operation.equals("LOCK") ? "locked" : "unlocked") + "!"));
    }

    @GetMapping("api/security/events/")
    public ResponseEntity<List<SecurityLog>> getSecurityEvents() {
        return ResponseEntity.ok(securityService.getSecurityLogs());
    }

    public UserResponse formatUserAsResponse(AccountUser user) {
        return new UserResponse(user.getId(), user.getName(), user.getLastname(), user.getEmail(), user.getFormattedAndSortedRolesAsStrings());
    }

    public PayrollResponse formatPayrollAsResponse(Payroll payroll) {
        if (payroll == null) {
            return null;
        }

        String period = payroll.getPeriod().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "-" + payroll.getPeriod().getYear();
        long dollars = payroll.getSalary() / 100;
        long cents = payroll.getSalary() % 100;
        String salary = dollars + " dollar(s) " + cents + " cent(s)";
        AccountUser user = accountService.getUserFromEmail(payroll.getEmployee());
        return new PayrollResponse(user.getName(), user.getLastname(), period, salary);
    }

    public record UserResponse(long id, String name, String lastname, String email, String[] roles) {
    }

    public record ChangePasswordRequest(@NotNull String new_password) {
    }

    public record UpdatedPasswordResponse(String email, String status) {
    }

    public record StatusResponse(String status) {
    }

    public record PayrollResponse(String name, String lastname, String period, String salary) {
    }

    public record DeletedUserResponse(String user, String status) {
    }

    public record ChangeRoleRequest(String user, String role, String operation) {
    }

    public record LockOrUnlockUserRequest(String user, String operation) {
    }
}
