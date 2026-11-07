package kh.edu.istad.platform.iam.infrastructure.oidc;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
/** Maps a user to OIDC standard claims according to the granted scopes (spec E.2). */
public final class OidcClaimsMapper {
    private OidcClaimsMapper() {}

    public static Map<String, Object> claims(AuthUserResult a, Collection<String> scopes) {
        UserResult u = a.user();
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("sub", u.uuid().toString());
        if (scopes.contains("profile")) {
            put(c, "name", u.name()); put(c, "given_name", u.givenName()); put(c, "family_name", u.familyName());
            put(c, "middle_name", u.middleName()); put(c, "nickname", u.nickname()); put(c, "preferred_username", u.username());
            put(c, "profile", u.profileUrl()); put(c, "picture", u.pictureUrl()); put(c, "website", u.websiteUrl());
            put(c, "gender", u.gender()); put(c, "birthdate", u.birthdate() == null ? null : u.birthdate().toString());
            put(c, "zoneinfo", u.zoneinfo()); put(c, "locale", u.locale());
            if (u.updatedAt() != null) c.put("updated_at", u.updatedAt().getEpochSecond());
        }
        if (scopes.contains("email")) { c.put("email", u.email()); c.put("email_verified", u.emailVerified()); }
        if (scopes.contains("phone")) {
            put(c, "phone_number", u.phoneNumber()); c.put("phone_number_verified", u.phoneNumberVerified());
        }
        if (scopes.contains("address") && u.address() != null) {
            AddressResult ad = u.address();
            Map<String, Object> m = new LinkedHashMap<>();
            put(m, "street_address", ad.streetAddress()); put(m, "locality", ad.locality());
            put(m, "region", ad.region()); put(m, "postal_code", ad.postalCode()); put(m, "country", ad.country());
            if (!m.isEmpty()) c.put("address", m);
        }
        return c;
    }
    private static void put(Map<String, Object> m, String k, Object v) { if (v != null) m.put(k, v); }
}
