package account_service.service;

import account_service.dto.LockRequest;
import account_service.dto.RoleDTO;
import account_service.dto.UserDTO;
import account_service.enums.LockingOperation;
import account_service.exception.InvalidEmployeeException;
import account_service.exception.InvalidInputException;
import account_service.exception.NotFoundException;
import account_service.mapper.UserMapper;
import account_service.model.Group;
import account_service.model.User;
import account_service.repository.FailedLoginAttemptRepository;
import account_service.repository.GroupRepository;
import account_service.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final GroupRepository groupRepository;
    private final FailedLoginAttemptRepository failedLoginAttemptRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository
                .findAllByOrderById()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public void deleteUser(@Email(regexp = ".*@acme\\.com$") String email) {

        User user = userRepository.findUserByUsernameIgnoreCase(email).orElseThrow(NotFoundException::new);
        if (user.getUserGroups().stream().anyMatch(group -> group.getCode().equals("ROLE_ADMINISTRATOR"))) {
            throw new InvalidEmployeeException("Can't remove ADMINISTRATOR role!");
        }
        userRepository.delete(user);
    }

    public UserDTO manageRoles(RoleDTO roleDTO) {

        Group role = groupRepository.findByCode("ROLE_" + roleDTO.getRole())
                .orElseThrow(() -> new NotFoundException("Role not found!"));
        User user = userRepository.findUserByUsernameIgnoreCase(roleDTO.getUser())
                .orElseThrow(() -> new NotFoundException("User not found!"));

        switch (roleDTO.getOperation()) {
            case "GRANT":
                grantRoleToUser(user, role);
                break;
            case "REMOVE":
                removeRoleFromUser(user, role);
                break;
            default:
                throw new IllegalStateException("Unsupported operation: " + roleDTO.getOperation());
        }
        return userMapper.toDTO(userRepository.save(user));
    }

    private void removeRoleFromUser(User user, Group role) {
        if (role.equals(groupRepository.findByCode("ROLE_ADMINISTRATOR").orElseThrow(() -> new NotFoundException("Role not found")))) {
            throw new InvalidEmployeeException("Can't remove ADMINISTRATOR role!");
        }

        boolean hasRole = user.getUserGroups().stream().anyMatch(group -> group.equals(role));

        if (!hasRole) {
            throw new InvalidEmployeeException("The user does not have a role!");
        }

        if (user.getUserGroups().size() == 1) {
            throw new InvalidEmployeeException("The user must have at least one role!");
        }

        user.getUserGroups().remove(role);
    }

    private void grantRoleToUser(User user, Group role) {
        if (combines(user, role)) {
            throw new InvalidInputException("The user cannot combine administrative and business roles!");
        }
        user.getUserGroups().add(role);
    }

    private boolean combines(User user, Group role) {
        return user.getUserGroups().stream().noneMatch(group -> group.getRole().equals(role.getRole()));
    }

    public void manageAccess(@Valid LockRequest request) {
        User user = userRepository.findUserByUsernameIgnoreCase(request.getUser())
                .orElseThrow(() -> new NotFoundException("User not found!"));
        if (request.getOperation().equals(LockingOperation.LOCK)) {
            if (user.getUserGroups().stream().anyMatch(group -> group.getCode().equals("ROLE_ADMINISTRATOR"))) {
                throw new InvalidInputException("Can't lock the ADMINISTRATOR!");
            }
            user.setLocked(true);
        } else {
            user.setLocked(false);
            failedLoginAttemptRepository.findByEmail(user.getUsername())
                    .ifPresent(a -> {
                        a.setAttempts(0);
                        failedLoginAttemptRepository.save(a);
                    });
        }
        userRepository.save(user);
    }
}
