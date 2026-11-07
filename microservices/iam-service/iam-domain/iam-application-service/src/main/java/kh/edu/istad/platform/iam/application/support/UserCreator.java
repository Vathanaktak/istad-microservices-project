package kh.edu.istad.platform.iam.application.support;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Component @RequiredArgsConstructor
public class UserCreator {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordHasher hasher;
    private final UserRoleAssigner roleAssigner;

    public User create(NewUser n) {
        String username = n.username().trim();
        String email = n.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByUsername(username)) throw new ConflictException("username-taken", "Username already taken");
        if (users.existsByEmail(email)) throw new ConflictException("email-taken", "Email already registered");
        if (n.phoneNumber() != null && users.existsByPhoneNumber(n.phoneNumber()))
            throw new ConflictException("phone-taken", "Phone number already registered");

        User u = new User();
        u.setUsername(username); u.setEmail(email); u.setEmailVerified(n.emailVerified());
        u.setGivenName(n.givenName()); u.setFamilyName(n.familyName()); u.setGender(n.gender()); u.setBirthdate(n.birthdate());
        u.setPhoneNumber(n.phoneNumber()); u.setZoneinfo(n.zoneinfo()); u.setPictureUrl(n.pictureUrl());
        u.setLocale(n.locale() == null ? "en" : n.locale());
        if (n.address() != null) {
            u.setAddressStreetAddress(n.address().streetAddress()); u.setAddressLocality(n.address().locality());
            u.setAddressRegion(n.address().region()); u.setAddressPostalCode(n.address().postalCode());
            u.setAddressCountry(n.address().country());
        }
        if (n.rawPassword() != null) u.changePassword(hasher.hash(n.rawPassword()));
        u = users.save(u);

        if (n.roleUuids() != null && !n.roleUuids().isEmpty()) {
            roleAssigner.assign(u, n.roleUuids(), null, "Initial assignment");
        } else {
            Role def = roles.findByName("USER").orElse(null);
            if (def != null) roleAssigner.assign(u, List.of(def.getUuid()), null, "Default role");
        }
        return u;
    }
}
