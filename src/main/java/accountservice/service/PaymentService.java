package accountservice.service;

import accountservice.dto.PaymentDTO;
import accountservice.dto.UserDTO;
import accountservice.exception.InvalidEmployeeException;
import accountservice.exception.InvalidInputException;
import accountservice.exception.PaymentNotFoundException;
import accountservice.mapper.PaymentMapper;
import accountservice.model.Payment;
import accountservice.model.User;
import accountservice.repository.PaymentRepository;
import accountservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final UserService userService;
    private static final String[] MONTHS = {"January", "February", "March", "April", "May",
            "June", "July", "August", "September", "October", "November", "December"};

    @Transactional
    public void uploadPayments(List<PaymentDTO> paymentDTOList) {
        for (PaymentDTO paymentDTO : paymentDTOList) {
            if (!paymentDTO.getPeriod().matches("^(0[1-9]|1[0-2])-(\\d{4})$")) {
                throw new InvalidInputException();
            }
            User user = userRepository
                    .findUserByUsernameIgnoreCase(paymentDTO.getEmployee())
                    .orElseThrow(InvalidEmployeeException::new);
            savePayment(paymentDTO, user);
        }
    }

    public void savePayment(PaymentDTO paymentDTO, User user) {
        if (paymentRepository.findByEmployeeIdOrderByPeriodDesc(user.getId()).stream()
                .anyMatch(x -> x.getPeriod().equals(paymentDTO.getPeriod()))) {
            throw new InvalidInputException("Invalid period for employee " + user.getUsername() + ".");
        }
        Payment payment = paymentMapper.toEntity(paymentDTO);
        paymentRepository.save(payment);
    }

    @Transactional
    public void updatePayment(PaymentDTO paymentDTO) {
        User user = userRepository
                .findUserByUsernameIgnoreCase(paymentDTO.getEmployee())
                .orElseThrow(InvalidEmployeeException::new);
        Payment payment = paymentRepository
                .findByEmployeeIdAndPeriod(user.getId(), paymentDTO.getPeriod())
                .orElseThrow(InvalidInputException::new);
        payment.setSalary(paymentDTO.getSalary());
        paymentRepository.save(payment);
    }

    public BigDecimal getPaymentForPeriod(String period, UserDTO userDTO) {
        return paymentRepository.findByEmployeeIdAndPeriod(userDTO.getId(), period)
                .orElseThrow(PaymentNotFoundException::new)
                .getSalary();
    }

    @Cacheable("payments")
    public List<PaymentDTO> getPayments(UserDTO userDTO) {
        List<PaymentDTO> list = new ArrayList<>();
        paymentRepository.findByEmployeeIdOrderByPeriodDesc(userService.getIdByUsername(userDTO.getUsername()))
                .forEach(p -> list.add(paymentMapper.toDTO(p)));
        return list;
    }

    public String salaryToString(BigDecimal salary) {
        BigDecimal[] parts = salary.divideAndRemainder(BigDecimal.valueOf(100));
        return String.format("%d dollar(s) %d cent(s)", parts[0].intValueExact(), parts[1].intValueExact());
    }

    public String formatPeriod(String period) {
        String[] separated = period.split("-");
        return MONTHS[Integer.parseInt(separated[0]) - 1] +
                "-" +
                separated[1];
    }
}
