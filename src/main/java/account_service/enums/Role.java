package account_service.enums;

public enum Role {
    ROLE_ADMINISTRATOR,
    ROLE_USER,
    ROLE_AUDITOR,
    ROLE_ACCOUNTANT;

    public String getWithoutPrefix() {
        return this.name().replace("ROLE_", "");
    }

}
