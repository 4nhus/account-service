package account.security;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface SecurityLogRepository extends ListPagingAndSortingRepository<SecurityLog, Long>, CrudRepository<SecurityLog, Long> {
}
