package account.security;

import org.springframework.data.repository.CrudRepository;

public interface IncorrectPasswordRecordRepository extends CrudRepository<IncorrectPasswordRecord, Long> {
    IncorrectPasswordRecord findByUsernameIgnoreCase(String username);
}
