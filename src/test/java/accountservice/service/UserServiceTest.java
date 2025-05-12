package accountservice.service;

import accountservice.dto.UserDTO;
import accountservice.enums.Role;
import accountservice.event.SecurityEventPublisher;
import accountservice.exception.HackedPasswordException;
import accountservice.exception.NotFoundException;
import accountservice.exception.SamePasswordException;
import accountservice.exception.UserExistsException;
import accountservice.mapper.UserMapper;
import accountservice.model.FailedLoginAttempt;
import accountservice.model.Group;
import accountservice.model.HackedPassword;
import accountservice.model.User;
import accountservice.repository.FailedLoginAttemptRepository;
import accountservice.repository.GroupRepository;
import accountservice.repository.HackedPasswordRepository;
import accountservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HackedPasswordRepository hackedPasswordRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private FailedLoginAttemptRepository failedLoginAttemptRepository;

    @Mock
    private SecurityEventPublisher securityEventPublisher;

    @InjectMocks
    private UserService userService;

    @Test
    void testSignup_UserExists_ThrowsUserExistsException() {
        UserDTO dto = new UserDTO();
        dto.setUsername("existingUser");
        dto.setPassword("password1234");

        when(userRepository.findUserByUsernameIgnoreCase("existingUser")).thenReturn(Optional.of(new User()));

        assertThrows(UserExistsException.class, () -> userService.signup(dto));
    }

    @Test
    void testSignup_HackedPassword_ThrowsHackedPasswordException() {
        UserDTO dto = new UserDTO();
        dto.setUsername("newUser");
        dto.setPassword("password1234");

        when(userRepository.findUserByUsernameIgnoreCase("newUser")).thenReturn(Optional.empty());
        when(hackedPasswordRepository.findByPassword("password1234")).thenReturn(Optional.of(new HackedPassword()));

        assertThrows(HackedPasswordException.class, () -> userService.signup(dto));
    }

    @Test
    void testSignup_CreatesUserWithCorrectGroup() {
        UserDTO dto = new UserDTO();
        dto.setUsername("newUser");
        dto.setPassword("password1234");
        Group adminGroup = new Group();
        adminGroup.setCode(Role.ROLE_ADMINISTRATOR.name());

        when(userRepository.findUserByUsernameIgnoreCase("newUser")).thenReturn(Optional.empty());
        when(hackedPasswordRepository.findByPassword("password1234")).thenReturn(Optional.empty());
        when(groupRepository.findByCode(Role.ROLE_ADMINISTRATOR.name())).thenReturn(Optional.of(adminGroup));
        when(userMapper.toEntity(dto)).thenReturn(new User());
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(userMapper.toDTO(any(User.class))).thenReturn(dto);

        UserDTO createdUser = userService.signup(dto);

        assertNotNull(createdUser);
        assertEquals("newUser", createdUser.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testFindByUsername_UserFound() {
        String username = "existingUser";
        User user = new User();
        user.setUsername(username);
        UserDTO dto = new UserDTO();
        dto.setUsername("existingUser");
        dto.setPassword("Password1234");

        when(userRepository.findUserByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(dto);

        UserDTO foundUser = userService.findByUsername(username);

        assertNotNull(foundUser);
        assertEquals(username, foundUser.getUsername());
    }

    @Test
    void testFindByUsername_UserNotFound() {
        String username = "nonExistingUser";

        when(userRepository.findUserByUsernameIgnoreCase(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findByUsername(username));
    }

    @Test
    void testUpdatePassword_HackedPassword_ThrowsHackedPasswordException() {
        String username = "user";
        String newPassword = "password123";

        when(hackedPasswordRepository.findByPassword(newPassword)).thenReturn(Optional.of(new HackedPassword()));

        assertThrows(HackedPasswordException.class, () -> userService.updatePassword(username, newPassword));
    }

    @Test
    void testUpdatePassword_SamePassword_ThrowsSamePasswordException() {
        String username = "user";
        String newPassword = "oldPassword";

        User user = new User();
        user.setPassword(newPassword);

        when(userRepository.findUserByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(true);

        assertThrows(SamePasswordException.class, () -> userService.updatePassword(username, newPassword));
    }

    @Test
    void testHandleFailedLogin_LockUserAfter5Attempts() {
        String email = "user@example.com";
        String path = "/login";
        FailedLoginAttempt failedLoginAttempt = new FailedLoginAttempt(null, email, 5);

        User user = new User();
        user.setUsername(email);
        user.setLocked(false);

        when(failedLoginAttemptRepository.findByEmail(email)).thenReturn(Optional.of(failedLoginAttempt));
        when(userRepository.findUserByUsernameIgnoreCase(email)).thenReturn(Optional.of(user));

        userService.handleFailedLogin(email, path);

        assertTrue(user.isLocked());
        verify(securityEventPublisher, times(3)).publishEvent(any(), any(), any(), any());
    }
}
