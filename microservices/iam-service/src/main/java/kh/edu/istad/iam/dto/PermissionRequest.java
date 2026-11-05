package kh.edu.istad.iam.dto;


public record PermissionRequest(
        String permission,
        String description
) {
}