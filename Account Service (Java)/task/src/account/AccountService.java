package account;

import account.exceptions.*;
import account.group.Group;
import account.group.GroupRepository;
import account.payroll.Payroll;
import account.payroll.PayrollID;
import account.payroll.PayrollRepository;
import account.security.SecurityEvent;
import account.user.AccountUser;
import account.user.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final PayrollRepository payrollRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher eventPublisher;

    public AccountService(UserRepository userRepository, PayrollRepository payrollRepository, GroupRepository groupRepository, PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.payrollRepository = payrollRepository;
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    public AccountUser getUserFromEmail(String email) {
        return userRepository.findUserByEmailIgnoreCase(email).orElse(null);
    }

    public AccountUser addUser(AccountUser user, String auth) {
        if (userRepository.findUserByEmailIgnoreCase(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        boolean isAdministratorAlreadyAdded = groupRepository.getAdminGroup().getUsersInGroup().size() == 1;

        if (isAdministratorAlreadyAdded) {
            Group users = groupRepository.getUserGroup();
            users.getUsersInGroup().add(user);
            user.getRoles().add(users);
            groupRepository.save(users);
        } else {
            Group admin = groupRepository.getAdminGroup();
            admin.getUsersInGroup().add(user);
            user.getRoles().add(admin);
            groupRepository.save(admin);
        }

        eventPublisher.publishEvent(new SecurityEvent(this, LocalDate.now(), SecurityEvent.Action.CREATE_USER, auth == null ? "Anonymous" : auth, user.getEmail(), "/api/auth/signup"));

        return getUserFromEmail(user.getEmail());
    }

    public void changeUserPassword(String email, String newPassword) {
        AccountUser user = getUserFromEmail(email);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new SamePasswordException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        eventPublisher.publishEvent(new SecurityEvent(this, LocalDate.now(), SecurityEvent.Action.CHANGE_PASSWORD, email, email, "/api/auth/changepass"));
    }

    @Transactional
    public void addPayrolls(List<Payroll> payrolls) {
        payrolls.forEach(payroll -> {
            if (payroll.getSalary() < 0) {
                throw new NegativeSalaryException();
            }

            if (getUserFromEmail(payroll.getEmployee()) == null) {
                throw new UserDoesNotExistException();
            }

            payroll.setEmployee(payroll.getEmployee().toLowerCase());

            if (payrollRepository.existsById(new PayrollID(payroll.getEmployee(), payroll.getPeriod()))) {
                throw new PayrollAlreadyExistsException();
            }

            payrollRepository.save(payroll);
        });
    }

    public void updatePayroll(Payroll payroll) {
        if (payroll.getSalary() < 0) {
            throw new NegativeSalaryException();
        }

        if (getUserFromEmail(payroll.getEmployee()) == null) {
            throw new UserDoesNotExistException();
        }

        Payroll updatedPayroll = payrollRepository.findById(new PayrollID(payroll.getEmployee().toLowerCase(), payroll.getPeriod())).get();

        updatedPayroll.setSalary(payroll.getSalary());
        payrollRepository.save(updatedPayroll);
    }


    public Optional<Payroll> getPayrollForUser(String email, LocalDate period) {
        return payrollRepository.findById(new PayrollID(email, period));
    }

    public List<Payroll> getAllPayrollsForUser(String email) {
        return payrollRepository.getPayrollsByEmployeeIgnoreCaseOrderByPeriodDesc(email);
    }

    public List<AccountUser> getUsers() {
        return userRepository.getAllUsersWithAscendingIds();
    }

    public void deleteUser(String email, String auth) {
        AccountUser user = getUserFromEmail(email);

        if (user.getRoles().contains(groupRepository.getAdminGroup())) {
            throw new DeleteAdminException("/api/admin/user/" + email);
        }

        List<Group> groups = groupRepository.findGroupsByUsersInGroupContains(user);

        groups.forEach(g -> {
            g.getUsersInGroup().remove(user);
            user.getRoles().remove(g);
            groupRepository.save(g);
        });

        userRepository.delete(user);

        eventPublisher.publishEvent(new SecurityEvent(this, LocalDate.now(), SecurityEvent.Action.DELETE_USER, auth, email, "/api/admin/user/"));
    }

    public AccountUser changeUserRole(String email, Group.Role role, String operation, String auth) {
        AccountUser user = getUserFromEmail(email);

        if (user == null) {
            throw new UserNotFoundException("/api/admin/user/role");
        }

        Group group = switch (role) {
            case ADMINISTRATOR -> groupRepository.getAdminGroup();
            case USER -> groupRepository.getUserGroup();
            case ACCOUNTANT -> groupRepository.getAccountantGroup();
            case AUDITOR -> groupRepository.getAuditorGroup();
        };

        if (operation.equals("GRANT")) {
            if (user.isAdmin() && (role == Group.Role.ACCOUNTANT || role == Group.Role.USER || role == Group.Role.AUDITOR)) {
                throw new CombineAdminBusinessRoleException();
            } else if (!user.isAdmin() && role == Group.Role.ADMINISTRATOR) {
                throw new CombineAdminBusinessRoleException();
            }

            group.getUsersInGroup().add(user);
            user.getRoles().add(group);

            eventPublisher.publishEvent(new SecurityEvent(this, LocalDate.now(), SecurityEvent.Action.GRANT_ROLE, auth, "Grant role " + role.name() + " to " + email.toLowerCase(), "/api/admin/user/role"));
        } else {
            if (!user.getRolesAsEnumSet().contains(role)) {
                throw new UserDoesNotHaveRoleException();
            }

            if (user.isAdmin()) {
                throw new DeleteAdminRoleException();
            }

            if (user.getRoles().size() == 1) {
                throw new DeleteUserOnlyRoleException();
            }

            group.getUsersInGroup().remove(user);
            user.getRoles().remove(group);

            eventPublisher.publishEvent(new SecurityEvent(this, LocalDate.now(), SecurityEvent.Action.REMOVE_ROLE, auth, "Remove role " + role.name() + " from " + email.toLowerCase(), "/api/admin/user/role"));
        }

        groupRepository.save(group);

        return user;
    }
}
