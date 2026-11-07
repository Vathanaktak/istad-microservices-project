package kh.edu.istad.platform.iam.application.result;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*;
import java.time.*; import java.util.*;
public record UserResult(UUID uuid, String username, String email, boolean emailVerified, String name, String givenName,
        String familyName, String middleName, String nickname, String gender, LocalDate birthdate, String profileUrl,
        String pictureUrl, String websiteUrl, String zoneinfo, String locale, String phoneNumber, boolean phoneNumberVerified,
        AddressResult address, boolean enabled, boolean accountNonLocked, List<RoleRef> roles, Instant createdAt, Instant updatedAt) {}
