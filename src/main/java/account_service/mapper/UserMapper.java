package account_service.mapper;

import account_service.dto.UserDTO;
import account_service.model.Group;
import account_service.model.User;
import org.springframework.stereotype.Component;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserMapper() {
    }

    public UserDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        } else {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(entity.getId());
            userDTO.setUsername(entity.getUsername());
            userDTO.setName(entity.getName());
            userDTO.setLastName(entity.getLastName());
            userDTO.setPassword(entity.getPassword());
            userDTO.setRoles(entity.getUserGroups().stream()
                    .map(Group::getCode)
                    .collect(Collectors.toCollection(TreeSet::new)));
            userDTO.setLocked(entity.isLocked());
            return userDTO;
        }
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        } else {
            User user = new User();
            user.setId(dto.getId());
            user.setUsername(dto.getUsername());
            user.setName(dto.getName());
            user.setLastName(dto.getLastName());
            user.setPassword(dto.getPassword());
            user.setLocked(dto.isLocked());

            return user;
        }
    }
}

