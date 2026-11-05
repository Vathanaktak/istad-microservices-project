package kh.edu.istad.iam.dto;


import kh.edu.istad.iam.domain.Permission;
import kh.edu.istad.iam.domain.Role;

import java.time.LocalDate;
import java.util.Set;

public record UserResponse(
        Long id,
        String uuid,
        String username,
        String email,
        boolean emailVerified,
        String familyName,
        String givenName,
        String gender,
        String picture,
        String cover,
        LocalDate birthdate,
        String phoneNumber,
        boolean phoneNumberVerified,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired,
        boolean enabled,
        Set<Permission> permissionIds,
        Set<Role> roleIds
) {
}