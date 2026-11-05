package kh.edu.istad.iam.dto;

public record PermissionResponse(
        Long id,
        String permission,
        String description
) {
}