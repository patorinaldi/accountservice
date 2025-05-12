package accountservice.repository;

import accountservice.model.HackedPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HackedPasswordRepository extends JpaRepository<HackedPassword, Long> {
    Optional<HackedPassword> findByPassword(String password);
}
