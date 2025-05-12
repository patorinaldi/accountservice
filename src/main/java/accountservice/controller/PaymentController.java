package accountservice.controller;

import accountservice.dto.EmployeePaymentDTO;
import accountservice.dto.PaymentDTO;
import accountservice.dto.ResponseDTO;
import accountservice.dto.UserDTO;
import accountservice.exception.InvalidInputException;
import accountservice.service.PaymentService;
import accountservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


@RestController
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/api/acct/payments")
    public ResponseEntity<ResponseDTO> uploadPayments(@Valid @RequestBody List<PaymentDTO> payments) {
        paymentService.uploadPayments(payments);
        ResponseDTO responseDTO = ResponseDTO.builder()
                .status("Added successfully!")
                .build();
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/api/acct/payments")
    public ResponseEntity<ResponseDTO> updatePayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        paymentService.updatePayment(paymentDTO);
        ResponseDTO responseDTO = ResponseDTO.builder()
                .status("Updated successfully!")
                .build();
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/api/empl/payment")
    public ResponseEntity<?> getPayment(@RequestParam(name = "period", required = false) String period) {
        UserDTO userDTO = userService.getCurrentUserDTO();
        if (period != null) {
            if (!period.matches("^(0[1-9]|1[0-2])-(\\d{4})$")) {
                throw new InvalidInputException();
            }
            EmployeePaymentDTO dto = EmployeePaymentDTO.builder()
                    .name(userDTO.getName())
                    .lastname(userDTO.getLastName())
                    .period(paymentService.formatPeriod(period))
                    .salary(paymentService.salaryToString(paymentService.getPaymentForPeriod(period, userDTO)))
                    .build();
            return ResponseEntity.ok(dto);

        }
        List<EmployeePaymentDTO> list = new ArrayList<>();
        paymentService.getPayments(userDTO).forEach(paymentDTO -> {
            EmployeePaymentDTO dto = EmployeePaymentDTO.builder()
                    .name(userDTO.getName())
                    .lastname(userDTO.getLastName())
                    .period(paymentService.formatPeriod(paymentDTO.getPeriod()))
                    .salary(paymentService.salaryToString(paymentDTO.getSalary()))
                    .build();
            list.add(dto);
        });
        return ResponseEntity.ok(list);
    }
}
