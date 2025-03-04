package account_service.service;

import account_service.enums.SecurityEventType;
import account_service.event.SecurityEventPublisher;
import account_service.exception.*;
import account_service.model.FailedLoginAttempt;
import account_service.model.Group;
import account_service.repository.FailedLoginAttemptRepository;
import account_service.repository.GroupRepository;
import account_service.repository.HackedPasswordRepository;
import account_service.model.User;
import account_service.dto.UserDTO;
import account_service.mapper.UserMapper;
import account_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("userService")
@AllArgsConstructor

public class UserService {

    private UserRepository userRepository;
    private UserMapper mapper;
    private PasswordEncoder encoder;
    private HackedPasswordRepository hackedPasswordRepository;
    private GroupRepository groupRepository;
    private FailedLoginAttemptRepository failedLoginAttemptRepository;
    private SecurityEventPublisher securityEventPublisher;

    public UserDTO signup(UserDTO dto) {
        if (userRepository.findUserByUsernameIgnoreCase(dto.getUsername()).isPresent()) {
            throw new UserExistsException();
        }
        if (hackedPasswordRepository.findByPassword(dto.getPassword()).isPresent()) {
            throw new HackedPasswordException();
        }
        User user = mapper.toEntity(dto);
        user.setUsername(dto.getUsername().toLowerCase());
        user.setPassword(encoder.encode(dto.getPassword()));
        if (userRepository.count() == 0) {
            Group group = groupRepository.findByCode("ROLE_ADMINISTRATOR").orElseThrow(() -> new NotFoundException("Role not found!"));
            user.getUserGroups().add(group);
        } else {
            Group group = groupRepository.findByCode("ROLE_USER").orElseThrow(() -> new NotFoundException("Role not found!"));
            user.getUserGroups().add(group);
        }
        return mapper.toDTO(userRepository.save(user));
    }

    @Cacheable("users")
    public UserDTO findByUsername(@Email String username) {
        return mapper.toDTO(userRepository.findUserByUsernameIgnoreCase(username).orElseThrow(NotFoundException::new));
    }

    public UserDTO getCurrentUserDTO() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return findByUsername(authentication.getName());
    }

    public void updatePassword(UserDTO userDTO, String password) {
        if (hackedPasswordRepository.findByPassword(password).isPresent()) {
            throw new HackedPasswordException();
        }
        if (encoder.matches(password, userDTO.getPassword())) {
            throw new SamePasswordException();
        }
        User user = userRepository.getReferenceById(userDTO.getId());
        user.setPassword(encoder.encode(password));
        userRepository.save(user);
    }

    public Long getIdByUsername(String username) {
        return userRepository.findUserByUsernameIgnoreCase(username)
                .orElseThrow(InvalidEmployeeException::new)
                .getId();
    }

    @Transactional
    public void handleFailedLogin(String email, String path) {
        FailedLoginAttempt attempt = failedLoginAttemptRepository.findByEmail(email)
                .orElse(new FailedLoginAttempt(null, email, 0));

        securityEventPublisher.publishEvent(SecurityEventType.LOGIN_FAILED, email, path, path);

        Optional<User> optionalUser = userRepository.findUserByUsernameIgnoreCase(email);

        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        if (user.getUserGroups().stream().anyMatch(g -> g.getCode().equals("ROLE_ADMINISTRATOR"))) {
            return;
        }

        attempt.setAttempts(attempt.getAttempts() + 1);
        failedLoginAttemptRepository.save(attempt);

        if (attempt.getAttempts() >= 5) {
            user.setLocked(true);
            userRepository.save(user);
            securityEventPublisher.publishEvent(SecurityEventType.BRUTE_FORCE, email, path, path);
            securityEventPublisher.publishEvent(SecurityEventType.LOCK_USER, email, "Lock user " + email, path);
        }
    }

    @Transactional
    public void handleSuccessfulLogin(String email) {
        failedLoginAttemptRepository.findByEmail(email).ifPresent(attempt -> {
            attempt.setAttempts(0);
            failedLoginAttemptRepository.save(attempt);
        });
    }
}
