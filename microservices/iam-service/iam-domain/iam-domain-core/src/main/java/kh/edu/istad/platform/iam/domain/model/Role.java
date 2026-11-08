package kh.edu.istad.platform.iam.domain.model;

import kh.edu.istad.platform.iam.domain.exception.BusinessRuleException;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Role extends Protectable {
    private RoleName name;

    // String accessor -- keeps MapStruct and all callers that do getName() working
    public String getName()         { return name == null ? null : name.value(); }
    public void   setName(String v) { this.name = RoleName.of(v); }

    public String authority()       { return name == null ? null : name.authority(); }

    // -------------------------------------------------------------------------
    // Domain Critical Logic
    // -------------------------------------------------------------------------

    /**
     * Validates and initialises a new role before persisting.
     * Mirrors the pattern used in Customer.initiateCustomer().
     */
    public void initiateRole() {
        validateNewRole();
        this.setActive(true);
    }

    private void validateNewRole() {
        if (getId() != null) {
            throw new BusinessRuleException("role-id-not-null", "Role id must be null before creation");
        }
        if (name == null || name.value() == null || name.value().isBlank()) {
            throw new BusinessRuleException("role-name-required", "Role name must not be blank");
        }
    }

    /**
     * Updates the role's name and/or description.
     * Throws if the role is system-protected.
     */
    public void updateRole(String newName, String description) {
        assertMutable();
        if (newName == null || newName.isBlank()) {
            throw new BusinessRuleException("role-name-required", "Role name must not be blank");
        }
        this.name = RoleName.of(newName);
        if (description != null) this.setDescription(description);
    }

    /**
     * Deactivates the role. Throws if already inactive or system-protected.
     */
    public void deactivateRole() {
        assertMutable();
        if (!isActive()) {
            throw new BusinessRuleException("role-already-inactive", "Role is already inactive");
        }
        this.setActive(false);
    }

    /**
     * Soft-deletes the role. Throws if system-protected or already deleted.
     */
    public void deleteRole(String actor) {
        assertMutable();
        if (isDeleted()) {
            throw new BusinessRuleException("role-already-deleted", "Role is already deleted");
        }
        softDelete(actor);
    }
}
