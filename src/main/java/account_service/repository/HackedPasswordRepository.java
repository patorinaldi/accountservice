package account_service.repository;

import account_service.model.HackedPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HackedPasswordRepository extends JpaRepository<HackedPassword, Long> {
    Optional<HackedPassword> findByPassword(String password);
}
