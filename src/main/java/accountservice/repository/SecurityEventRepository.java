package accountservice.repository;

import accountservice.model.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
}
