package account.group;

import account.user.AccountUser;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {
    Group findGroupByRole(Group.Role role);

    List<Group> findGroupsByUsersInGroupContains(AccountUser user);

    default boolean isInitialisedWithRoles() {
        return getAdminGroup() != null;
    }

    default Group getAdminGroup() {
        return findGroupByRole(Group.Role.ADMINISTRATOR);
    }

    default Group getAccountantGroup() {
        return findGroupByRole(Group.Role.ACCOUNTANT);
    }

    default Group getUserGroup() {
        return findGroupByRole(Group.Role.USER);
    }

    default Group getAuditorGroup() {
        return findGroupByRole(Group.Role.AUDITOR);
    }
}
