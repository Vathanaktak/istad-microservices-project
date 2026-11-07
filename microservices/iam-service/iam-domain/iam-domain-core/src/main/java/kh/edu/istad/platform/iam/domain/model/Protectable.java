package kh.edu.istad.platform.iam.domain.model;

import kh.edu.istad.platform.iam.domain.exception.BusinessRuleException;
import lombok.Getter;
import lombok.Setter;

/** Base for Role and Permission: system protection and active flag. */
@Getter @Setter
public abstract class Protectable extends Auditable {
    private String description;
    private boolean system;
    private boolean active = true;

    public void assertMutable() {
        if (system) throw new BusinessRuleException("system-protected", "System entries cannot be changed or deleted");
    }
    public void assertAssignable() {
        if (!active) throw new BusinessRuleException("inactive", "Inactive entries cannot be assigned");
    }
}
