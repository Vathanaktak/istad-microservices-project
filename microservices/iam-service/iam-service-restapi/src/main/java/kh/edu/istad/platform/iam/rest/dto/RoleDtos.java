package kh.edu.istad.platform.iam.rest.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class RoleDtos {
    private RoleDtos() {}

    public record CreateRoleRequest(
        @NotBlank @Size(max = 50) @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "UPPER_SNAKE_CASE") String name,
        @Size(max = 255) String description, List<UUID> permissionUuids) {}
    public record UpdateRoleRequest(@Size(max = 50) @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "UPPER_SNAKE_CASE") String name,
                                    @Size(max = 255) String description) {}
    public record AssignPermissionsToRoleRequest(@NotEmpty List<@NotNull UUID> permissionUuids, @Future Instant expiresAt,
                                                 @Size(max = 255) String reason) {}

    public record RoleSummaryResponse(UUID uuid, String name, String description, boolean system, boolean active) {}
    public record PermissionResponse(UUID uuid, String name, String resource, String action, String description, boolean system, boolean active) {}
    public record RoleResponse(UUID uuid, String name, String description, boolean system, boolean active,
                               List<PermissionResponse> permissions, Instant createdAt, Instant updatedAt) {}
    public record PermissionAssignmentResponse(UUID uuid, String name, Instant assignedAt, Instant expiresAt) {}
}
