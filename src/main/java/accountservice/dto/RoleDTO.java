package accountservice.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class RoleDTO {

    @Email
    private String user;
    private String role;
    private String operation;

}
