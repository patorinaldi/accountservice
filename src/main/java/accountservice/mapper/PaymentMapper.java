package accountservice.mapper;

import accountservice.dto.PaymentDTO;
import accountservice.exception.InvalidEmployeeException;
import accountservice.model.Payment;
import accountservice.repository.UserRepository;
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
