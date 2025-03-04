package account_service.dto;

import account_service.enums.LockingOperation;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LockRequest {
    @NotBlank
    private String user;
    private LockingOperation operation;
}

