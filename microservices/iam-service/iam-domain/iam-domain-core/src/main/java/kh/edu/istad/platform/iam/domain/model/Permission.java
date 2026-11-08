package kh.edu.istad.platform.iam.domain.model;

import kh.edu.istad.platform.iam.domain.exception.BusinessRuleException;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionName;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Permission extends Protectable {
    private PermissionName name;   // resource:action composite
    private String resource;
    private String action;

    // String accessor -- keeps getName() usages working (MapStruct, mappers)
    public String getName()         { return name == null ? null : name.value(); }
    public void   setName(String v) { this.name = PermissionName.of(v); }

    public static String nameOf(String resource, String action) {
        return PermissionName.of(resource, action).value();
    }

    // -------------------------------------------------------------------------
    // Domain Critical Logic
    // -------------------------------------------------------------------------

    /**
     * Validates and initialises a new permission before persisting.
     * Mirrors the pattern used in Customer.initiateCustomer().
     */
    public void initiatePermission() {
        validateNewPermission();
        this.setActive(true);
    }

    private void validateNewPermission() {
        if (getId() != null) {
            throw new BusinessRuleException("permission-id-not-null", "Permission id must be null before creation");
        }
        if (resource == null || resource.isBlank()) {
            throw new BusinessRuleException("permission-resource-required", "Permission resource must not be blank");
        }
        if (action == null || action.isBlank()) {
            throw new BusinessRuleException("permission-action-required", "Permission action must not be blank");
        }
        // Rebuild the composite name from resource + action to ensure consistency
        this.name = PermissionName.of(resource, action);
    }

    /**
     * Updates the permission's resource, action, and/or description.
     * Throws if system-protected.
     */
    public void updatePermission(String resource, String action, String description) {
        assertMutable();
        if (resource == null || resource.isBlank()) {
            throw new BusinessRuleException("permission-resource-required", "Permission resource must not be blank");
        }
        if (action == null || action.isBlank()) {
            throw new BusinessRuleException("permission-action-required", "Permission action must not be blank");
        }
        this.resource = resource;
        this.action = action;
        this.name = PermissionName.of(resource, action);
        if (description != null) this.setDescription(description);
    }

    /**
     * Deactivates the permission. Throws if already inactive or system-protected.
     */
    public void deactivatePermission() {
        assertMutable();
        if (!isActive()) {
            throw new BusinessRuleException("permission-already-inactive", "Permission is already inactive");
        }
        this.setActive(false);
    }

    /**
     * Soft-deletes the permission. Throws if system-protected or already deleted.
     */
    public void deletePermission(String actor) {
        assertMutable();
        if (isDeleted()) {
            throw new BusinessRuleException("permission-already-deleted", "Permission is already deleted");
        }
        softDelete(actor);
    }
}
