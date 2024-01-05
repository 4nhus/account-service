package account.group;

import org.springframework.stereotype.Component;

@Component
public class GroupLoader {
    private final GroupRepository groupRepository;

    public GroupLoader(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;

        if (!groupRepository.isInitialisedWithRoles())
            initialiseRoles();
    }

    private void initialiseRoles() {
        groupRepository.save(new Group(Group.Role.ADMINISTRATOR));
        groupRepository.save(new Group(Group.Role.ACCOUNTANT));
        groupRepository.save(new Group(Group.Role.USER));
        groupRepository.save(new Group(Group.Role.AUDITOR));
    }
}
