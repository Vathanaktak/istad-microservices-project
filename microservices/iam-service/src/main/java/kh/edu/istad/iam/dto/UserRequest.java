package kh.edu.istad.iam.dto;


import java.time.LocalDate;
import java.util.Set;

public record UserRequest(
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
        String password,
        boolean phoneNumberVerified,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired,
        boolean enabled,
        Set<Long> permissionIds,
        Set<Long> roleIds
) {
}