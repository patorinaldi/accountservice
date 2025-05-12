package accountservice.controller;

import accountservice.dto.PasswordRequest;
import accountservice.dto.ResponseDTO;
import accountservice.dto.UserDTO;
import accountservice.enums.SecurityEventType;
import accountservice.event.SecurityEventPublisher;
import accountservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class UserController {

    private UserService userService;
    private SecurityEventPublisher securityEventPublisher;

    @PostMapping("/api/auth/signup")
    public ResponseEntity<UserDTO> signup(@Valid @RequestBody UserDTO dto) {
        UserDTO user = userService.signup(dto);

        securityEventPublisher.publishEvent(
                SecurityEventType.CREATE_USER,
                "Anonymous",
                user.getUsername(),
                "/api/auth/signup"
        );

        return ResponseEntity.ok(user);
    }

    @PostMapping("/api/auth/changepass")
    public ResponseEntity<ResponseDTO> changePass(@Valid @RequestBody PasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updatePassword(email, request.getNewPassword());
        ResponseDTO responseDTO = ResponseDTO.builder()
                .email(email)
                .status("The password has been updated successfully")
                .build();
        securityEventPublisher.publishEvent(
                SecurityEventType.CHANGE_PASSWORD,
                email,
                email,
                "/api/auth/changepass"
        );
        return ResponseEntity.ok(responseDTO);
    }

}

