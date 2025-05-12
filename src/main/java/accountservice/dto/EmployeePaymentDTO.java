package accountservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeePaymentDTO {
    private String name;
    private String lastname;
    private String period;
    private String salary;
}
