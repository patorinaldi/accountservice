package account_service.repository;

import account_service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByEmployeeIdOrderByPeriodDesc(Long id);
    Optional<Payment> findByEmployeeIdAndPeriod(Long id, String period);
}
