package accountservice.service;

import accountservice.dto.LockRequest;
import accountservice.dto.RoleDTO;
import accountservice.dto.UserDTO;
import accountservice.enums.LockingOperation;
import accountservice.enums.Role;
import accountservice.exception.InvalidEmployeeException;
import accountservice.exception.InvalidInputException;
import accountservice.exception.NotFoundException;
import accountservice.mapper.UserMapper;
import accountservice.model.Group;
import accountservice.model.User;
import accountservice.repository.FailedLoginAttemptRepository;
import accountservice.repository.GroupRepository;
import accountservice.repository.UserRepository;
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
        if (user.getUserGroups().stream().anyMatch(group -> group.getCode().equals(Role.ROLE_ADMINISTRATOR.name()))) {
            throw new InvalidEmployeeException("Administrator " + user.getUsername() + " cannot be deleted.");
        }
        userRepository.delete(user);
    }

    public UserDTO manageRoles(RoleDTO roleDTO) {

        Group role = groupRepository.findByCode("ROLE_" + roleDTO.getRole().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Role " + roleDTO + " not found."));
        User user = userRepository.findUserByUsernameIgnoreCase(roleDTO.getUser())
                .orElseThrow(() -> new NotFoundException("User " + roleDTO.getUser() + " not found."));

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
        if (role.equals(groupRepository.findByCode(Role.ROLE_ADMINISTRATOR.name()).orElseThrow(() -> new NotFoundException("Role not found")))) {
            throw new InvalidEmployeeException(role.getCode() + " cannot be removed");
        }

        boolean hasRole = user.getUserGroups().stream().anyMatch(group -> group.equals(role));

        if (!hasRole) {
            throw new InvalidEmployeeException("User " + user.getUsername() + " does not have " + role.getCode() + " role!");
        }

        if (user.getUserGroups().size() == 1) {
            throw new InvalidEmployeeException("The user " + user.getUsername() + " must have at least one role.");
        }

        user.getUserGroups().remove(role);
    }

    private void grantRoleToUser(User user, Group role) {
        if (isCrossCategoryAssignment(user, role)) {
            throw new InvalidInputException("User " + user.getUsername() + " cannot combine administrative and business roles.");
        }
        boolean hasRole = user.getUserGroups().stream().anyMatch(group -> group.equals(role));
        if (hasRole) {
            throw new InvalidEmployeeException("User " + user.getUsername() + " already have role " + role.getCode());
        }
        user.getUserGroups().add(role);
    }

    private boolean isCrossCategoryAssignment(User user, Group newGroup) {
        return user.getUserGroups().stream().noneMatch(group -> group.getRole().equals(newGroup.getRole()));
    }

    public void manageAccess(@Valid LockRequest request) {
        User user = userRepository.findUserByUsernameIgnoreCase(request.getUser())
                .orElseThrow(() -> new NotFoundException("User " + request.getUser() + "  not found!"));
        if (request.getOperation().equals(LockingOperation.LOCK)) {
            if (user.getUserGroups().stream().anyMatch(group -> group.getCode().equals(Role.ROLE_ADMINISTRATOR.name()))) {
                throw new InvalidInputException("Administrator " + user.getUsername() + " cannot be locked.");
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
