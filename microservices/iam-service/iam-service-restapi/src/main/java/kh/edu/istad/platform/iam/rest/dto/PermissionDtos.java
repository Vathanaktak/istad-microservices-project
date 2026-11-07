package kh.edu.istad.platform.iam.rest.dto;

import jakarta.validation.constraints.*;

public final class PermissionDtos {
    private PermissionDtos() {}
    public record CreatePermissionRequest(
        @NotBlank @Size(max = 50) @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_-]*$") String resource,
        @NotBlank @Size(max = 50) @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_-]*$") String action,
        @Size(max = 255) String description) {}
    public record UpdatePermissionRequest(@Size(max = 255) String description) {}
}
