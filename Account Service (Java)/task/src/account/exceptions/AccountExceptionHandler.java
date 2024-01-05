package account.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class AccountExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorMessage> handleUserAlreadyExists() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "User exist!", "/api/auth/signup"));
    }

    @ExceptionHandler(PasswordTooShortException.class)
    public ResponseEntity<ErrorMessage> handlePasswordTooShort(PasswordTooShortException e) {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "Password length must be 12 chars minimum!", e.getPath()));
    }

    @ExceptionHandler(BreachedPasswordException.class)
    public ResponseEntity<ErrorMessage> handleBreachedPassword(BreachedPasswordException e) {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "The password is in the hacker's database!", e.getPath()));
    }

    @ExceptionHandler(SamePasswordException.class)
    public ResponseEntity<ErrorMessage> handleSamePassword() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "The passwords must be different!", "/api/auth/changepass"));
    }

    @ExceptionHandler(NegativeSalaryException.class)
    public ResponseEntity<ErrorMessage> handleNegativeSalary() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "Salary cannot be negative for a payroll period!", "/api/acct/payments"));
    }

    @ExceptionHandler(UserDoesNotExistException.class)
    public ResponseEntity<ErrorMessage> handleUserDoesNotExist() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "Pay roll user does not exist!", "/api/acct/payments"));
    }

    @ExceptionHandler(PayrollAlreadyExistsException.class)
    public ResponseEntity<ErrorMessage> handlePayrollAlreadyExists() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "Payroll exist!", "/api/acct/payments"));
    }

    @ExceptionHandler(InvalidPeriodException.class)
    public ResponseEntity<ErrorMessage> handleInvalidPeriod() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "Period month is invalid!", "/api/empl/payment"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleUserNotFound(UserNotFoundException e) {
        return new ResponseEntity<>(new ErrorMessage(LocalDateTime.now(), HttpStatus.NOT_FOUND, "Not Found", "User not found!", e.getPath()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DeleteAdminException.class)
    public ResponseEntity<ErrorMessage> handleDeleteAdmin(DeleteAdminException e) {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "Can't remove ADMINISTRATOR role!", e.getPath()));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleRoleNotFound() {
        return new ResponseEntity<>(new ErrorMessage(LocalDateTime.now(), HttpStatus.NOT_FOUND, "Not Found", "Role not found!", "/api/admin/user/role"), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserDoesNotHaveRoleException.class)
    public ResponseEntity<ErrorMessage> handleUserDoesNotHaveRole() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "The user does not have a role!", "/api/admin/user/role"));
    }

    @ExceptionHandler(DeleteUserOnlyRoleException.class)
    public ResponseEntity<ErrorMessage> handleDeleteUserOnlyRole() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "The user must have at least one role!", "/api/admin/user/role"));
    }

    @ExceptionHandler(DeleteAdminRoleException.class)
    public ResponseEntity<ErrorMessage> handleDeleteAdminRole() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "Can't remove ADMINISTRATOR role!", "/api/admin/user/role"));
    }

    @ExceptionHandler(CombineAdminBusinessRoleException.class)
    public ResponseEntity<ErrorMessage> handleCombineAdminBusinessRole() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "The user cannot combine administrative and business roles!", "/api/admin/user/role"));
    }

    @ExceptionHandler(LockAdminException.class)
    public ResponseEntity<ErrorMessage> handleLockAdmin() {
        return ResponseEntity.badRequest().body(new ErrorMessage(LocalDateTime.now(), HttpStatus.BAD_REQUEST, "Bad Request", "Can't lock the ADMINISTRATOR!", "/api/admin/user/access"));
    }
}
