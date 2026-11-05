package kh.edu.istad.iam.dto;

import java.util.Set;

public record RoleRequest(
        String role,
        String description,
        Set<Long> permissionIds
) {
}