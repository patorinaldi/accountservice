package account_service.controller;

import account_service.dto.LockRequest;
import account_service.dto.ResponseDTO;
import account_service.dto.RoleDTO;
import account_service.dto.UserDTO;
import account_service.enums.LockingOperation;
import account_service.enums.SecurityEventType;
import account_service.event.SecurityEventPublisher;
import account_service.service.AdminService;
import account_service.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class AdminController {

    private AdminService adminService;
    private UserService userService;
    private SecurityEventPublisher securityEventPublisher;

    @GetMapping("/api/admin/user/")
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<UserDTO> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/api/admin/user/{email}")
    public ResponseEntity<ResponseDTO> deleteUser(@PathVariable("email") @Email String email) {
        adminService.deleteUser(email);

        ResponseDTO responseDTO = ResponseDTO
                .builder()
                .user(email)
                .status("Deleted successfully!")
                .build();

        securityEventPublisher.publishEvent(
                SecurityEventType.DELETE_USER,
                userService.getCurrentUserDTO().getUsername(),
                email,
                "api/admin/user"
        );

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/api/admin/user/role")
    public ResponseEntity<UserDTO> manageRoles(@RequestBody @Valid RoleDTO roleDTO) {

        UserDTO user = adminService.manageRoles(roleDTO);

        if (roleDTO.getOperation().equals("GRANT")) {
            securityEventPublisher.publishEvent(
                    SecurityEventType.GRANT_ROLE,
                    userService.getCurrentUserDTO().getUsername(),
                    "Grant role " + roleDTO.getRole() + " to " + roleDTO.getUser().toLowerCase(),
                    "api/admin/user/role"
            );
        } else {
            securityEventPublisher.publishEvent(
                    SecurityEventType.REMOVE_ROLE,
                    userService.getCurrentUserDTO().getUsername(),
                    "Remove role " + roleDTO.getRole() + " from " + roleDTO.getUser().toLowerCase(),
                    "api/admin/user/role"
            );
        }

        return ResponseEntity.ok(user);
    }

    @PutMapping("/api/admin/user/access")
    public ResponseEntity<?> manageUserAccess(@RequestBody @Valid LockRequest request) {
        adminService.manageAccess(request);
        ResponseDTO responseDTO;
        if (request.getOperation().equals(LockingOperation.LOCK)) {
            responseDTO = ResponseDTO.builder().status("User " + request.getUser().toLowerCase() + " locked!").build();
            securityEventPublisher.publishEvent(
                    SecurityEventType.LOCK_USER,
                    userService.getCurrentUserDTO().getUsername(),
                    "Lock user " + request.getUser().toLowerCase(),
                    "/api/admin/user/access"
            );
        } else {
            responseDTO = ResponseDTO.builder().status("User " + request.getUser().toLowerCase() + " unlocked!").build();
            securityEventPublisher.publishEvent(
                    SecurityEventType.UNLOCK_USER,
                    userService.getCurrentUserDTO().getUsername(),
                    "Unlock user " + request.getUser().toLowerCase(),
                    "/api/admin/user/access"
            );

        }
        return ResponseEntity.ok(responseDTO);
    }
}
