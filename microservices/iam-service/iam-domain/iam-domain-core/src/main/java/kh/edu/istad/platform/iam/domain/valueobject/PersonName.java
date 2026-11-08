package kh.edu.istad.platform.iam.domain.valueobject;

/**
 * Full name decomposed into given / family / middle / nickname parts.
 * Any component may be {@code null}.
 */
public record PersonName(
        String givenName,
        String familyName,
        String middleName,
        String nickname
) {

    /** Convenience factory when only given + family are known. */
    public static PersonName of(String givenName, String familyName) {
        return new PersonName(givenName, familyName, null, null);
    }

    public static PersonName of(String givenName, String familyName, String middleName, String nickname) {
        return new PersonName(givenName, familyName, middleName, nickname);
    }

    /** Returns "{givenName} {familyName}", trimmed. */
    public String fullName() {
        return ((givenName == null ? "" : givenName) + " " + (familyName == null ? "" : familyName)).trim();
    }
}
