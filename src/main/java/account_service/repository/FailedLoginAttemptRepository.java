package account_service.repository;

import account_service.model.FailedLoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FailedLoginAttemptRepository extends JpaRepository<FailedLoginAttempt, Long> {

    Optional<FailedLoginAttempt> findByEmail(String email);

}
