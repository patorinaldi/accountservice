package accountservice.dto;

import accountservice.enums.LockingOperation;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LockRequest {
    @NotBlank
    private String user;
    private LockingOperation operation;
}

