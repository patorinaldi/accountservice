package account_service.mapper;

import account_service.dto.PaymentDTO;
import account_service.exception.InvalidEmployeeException;
import account_service.model.Payment;
import account_service.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentMapper {

    private final UserRepository userRepository;

    public PaymentDTO toDTO(Payment payment) {
        return PaymentDTO.builder()
                .employee(userRepository
                        .findById(payment.getEmployeeId())
                        .orElseThrow(InvalidEmployeeException::new)
                        .getUsername())
                .period(payment.getPeriod())
                .salary(payment.getSalary())
                .build();
    }

    public Payment toEntity(PaymentDTO dto) {

        return Payment.builder()
                .employeeId(userRepository
                        .findUserByUsernameIgnoreCase(dto.getEmployee())
                        .orElseThrow(InvalidEmployeeException::new)
                        .getId())
                .period(dto.getPeriod())
                .salary(dto.getSalary())
                .build();
    }
}
