package accountservice.security;

import accountservice.enums.Role;
import accountservice.model.Group;
import accountservice.model.HackedPassword;
import accountservice.repository.GroupRepository;
import accountservice.repository.HackedPasswordRepository;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final GroupRepository groupRepository;
    private final HackedPasswordRepository hackedPasswordRepository;

    public DataLoader(GroupRepository groupRepository, HackedPasswordRepository hackedPasswordRepository) {
        this.groupRepository = groupRepository;
        this.hackedPasswordRepository = hackedPasswordRepository;
        createRoles();
        createPasswords();
    }

    private void createPasswords() {
        try {
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForJanuary"));
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForFebruary"));
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForMarch"));
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForApril"));
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForMay"));
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForJune"));
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForJuly"));
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForAugust"));
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForSeptember"));
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForOctober"));
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForNovember"));
            hackedPasswordRepository.save(new HackedPassword(null, "PasswordForDecember"));
        } catch (Exception _) {
        }
    }

    private void createRoles(){
        try {
            groupRepository.save(new Group(null, Role.ROLE_ADMINISTRATOR.name(), "Administrator Group", "administrative", null));
            groupRepository.save(new Group(null, Role.ROLE_AUDITOR.name(), "Auditor Group", "business", null));
            groupRepository.save(new Group(null,Role.ROLE_ACCOUNTANT.name(), "Accountant Group", "business", null));
            groupRepository.save(new Group(null, Role.ROLE_USER.name(), "User Group", "business", null));
        } catch (Exception _) {
        }
    }
}
