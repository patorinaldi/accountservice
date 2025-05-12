package accountservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {

    private Long id;
    @NotBlank
    @Email(regexp = ".*@acme\\.com$")
    @JsonProperty("email")
    private String username;
    @NotBlank
    private String name;
    @NotBlank
    @JsonProperty("lastname")
    private String lastName;
    @JsonProperty("locked")
    private boolean locked;
    @NotBlank
    @Size(min = 12, message = "Password length must be 12 chars minimum!")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    Set<String> roles;
}

