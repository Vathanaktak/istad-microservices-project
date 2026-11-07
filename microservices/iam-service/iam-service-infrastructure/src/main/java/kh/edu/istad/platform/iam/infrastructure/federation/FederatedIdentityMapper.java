package kh.edu.istad.platform.iam.infrastructure.federation;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
/** Normalises provider-specific user attributes into a FederatedIdentityCommand. */
public final class FederatedIdentityMapper {
    private FederatedIdentityMapper() {}

    public static FederatedIdentityCommand map(String registrationId, Map<String, Object> a) {
        IdentityProvider p = IdentityProvider.fromRegistrationId(registrationId);
        return switch (p) {
            case GOOGLE -> new FederatedIdentityCommand(p, str(a, "sub"), str(a, "email"), bool(a, "email_verified"),
                str(a, "name"), null, str(a, "picture"), str(a, "given_name"), str(a, "family_name"), locale(str(a, "locale")), a);
            case GITHUB -> {
                String login = str(a, "login"), id = str(a, "id"), email = str(a, "email");
                // GitHub only returns an e-mail when public; treat it as unverified
                yield new FederatedIdentityCommand(p, id, email, false, str(a, "name") != null ? str(a, "name") : login,
                    login, str(a, "avatar_url"), null, null, "en", a);
            }
            case FACEBOOK -> new FederatedIdentityCommand(p, str(a, "id"), str(a, "email"), false, str(a, "name"), null,
                null, null, null, "en", a);
            case TELEGRAM -> throw new IllegalArgumentException("Telegram uses TelegramLoginFilter");
        };
    }
    private static String str(Map<String, Object> a, String k) { Object v = a.get(k); return v == null ? null : String.valueOf(v); }
    private static boolean bool(Map<String, Object> a, String k) { return Boolean.parseBoolean(str(a, k)); }
    private static String locale(String l) { return l != null && l.toLowerCase().startsWith("km") ? "km" : "en"; }
}
