package kh.edu.istad.iam.dto;

import java.util.Set;

public record RoleResponse(
        Long id,
        String role,
        String description,
        Set<Long> permissionIds
) {
}