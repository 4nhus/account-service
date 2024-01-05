package account.user;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends ListPagingAndSortingRepository<AccountUser, Long>, CrudRepository<AccountUser, Long> {
    default List<AccountUser> getAllUsersWithAscendingIds() {
        return findAll(Sort.by("id"));
    }

    Optional<AccountUser> findUserByEmailIgnoreCase(String email);
}
