package account.user;

import account.group.Group;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "roles")
public class AccountUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotBlank
    private String name;
    @NotBlank
    private String lastname;
    @NotNull
    @Email(regexp = "\\w+@acme.com")
    private String email;
    @NotNull
    private String password;
    @ManyToMany(mappedBy = "usersInGroup", fetch = FetchType.EAGER)
    private Set<Group> roles = new LinkedHashSet<>();
    private boolean isLocked;

    public String[] getRolesAsStrings() {
        return roles.stream().map(Group::getAuthority).collect(Collectors.joining(",")).split(",");
    }

    public String[] getFormattedAndSortedRolesAsStrings() {
        return roles.stream().map(r -> "ROLE_" + r.getAuthority()).sorted().collect(Collectors.joining(",")).split(",");
    }

    public EnumSet<Group.Role> getRolesAsEnumSet() {
        return EnumSet.copyOf(roles.stream().map(Group::getRole).collect(Collectors.toList()));
    }

    public boolean isAdmin() {
        return getRolesAsEnumSet().contains(Group.Role.ADMINISTRATOR);
    }
}
