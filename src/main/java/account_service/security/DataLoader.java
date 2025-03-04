package account_service.security;

import account_service.model.Group;
import account_service.model.HackedPassword;
import account_service.repository.GroupRepository;
import account_service.repository.HackedPasswordRepository;
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
            groupRepository.save(new Group(null,"ROLE_ADMINISTRATOR", "Administrator Group", "administrative", null));
            groupRepository.save(new Group(null, "ROLE_AUDITOR", "Auditor Group", "business", null));
            groupRepository.save(new Group(null,"ROLE_ACCOUNTANT", "Accountant Group", "business", null));
            groupRepository.save(new Group(null,"ROLE_USER", "User Group", "business", null));
        } catch (Exception _) {
        }
    }
}
