package accountservice.service;

import accountservice.enums.Role;
import accountservice.enums.SecurityEventType;
import accountservice.event.SecurityEventPublisher;
import accountservice.exception.*;
import accountservice.model.FailedLoginAttempt;
import accountservice.model.Group;
import accountservice.repository.FailedLoginAttemptRepository;
import accountservice.repository.GroupRepository;
import accountservice.repository.HackedPasswordRepository;
import accountservice.model.User;
import accountservice.dto.UserDTO;
import accountservice.mapper.UserMapper;
import accountservice.repository.UserRepository;
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
    private UserMapper userMapper;
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
        User user = userMapper.toEntity(dto);
        user.setUsername(dto.getUsername().toLowerCase());
        user.setPassword(encoder.encode(dto.getPassword()));
        if (userRepository.count() == 0) {
            Group group = groupRepository.findByCode(Role.ROLE_ADMINISTRATOR.name()).orElseThrow(() -> new NotFoundException("Role not found!"));
            user.getUserGroups().add(group);
        } else {
            Group group = groupRepository.findByCode(Role.ROLE_USER.name()).orElseThrow(() -> new NotFoundException("Role not found!"));
            user.getUserGroups().add(group);
        }
        return userMapper.toDTO(userRepository.save(user));
    }

    @Cacheable("users")
    public UserDTO findByUsername(@Email String username) {
        return userMapper.toDTO(userRepository.findUserByUsernameIgnoreCase(username).orElseThrow(NotFoundException::new));
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

        if (user.getUserGroups().stream().anyMatch(g -> g.getCode().equals(Role.ROLE_ADMINISTRATOR.name()))) {
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
