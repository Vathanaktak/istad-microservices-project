package kh.edu.istad.platform.iam.domain.model;

import java.util.Locale;

public enum IdentityProvider {
    GOOGLE, GITHUB, FACEBOOK, TELEGRAM;

    public static IdentityProvider fromRegistrationId(
            String id) {
        return valueOf(id.toUpperCase(Locale.ROOT));
    }
}
