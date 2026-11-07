package kh.edu.istad.platform.iam.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class User extends Auditable {
    private String username;
    private String email;
    private boolean emailVerified;
    private String passwordHash;
    private String givenName;
    private String familyName;
    private String middleName;
    private String nickname;
    private String gender;
    private LocalDate birthdate;
    private String profileUrl;
    private String pictureUrl;
    private String websiteUrl;
    private String zoneinfo;
    private String locale = "en";
    private String phoneNumber;
    private boolean phoneNumberVerified;
    private String addressStreetAddress;
    private String addressLocality;
    private String addressRegion;
    private String addressPostalCode;
    private String addressCountry;
    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private int failedLoginAttempts;
    private Instant lastLoginAt;
    private Instant passwordChangedAt;

    public String fullName() {
        return ((givenName == null ? "" : givenName) + " " + (familyName == null ? "" : familyName)).trim();
    }
    public boolean hasPassword() { return passwordHash != null && !passwordHash.isBlank(); }

    public void changePassword(String hash) {
        this.passwordHash = hash;
        this.passwordChangedAt = Instant.now();
        this.credentialsNonExpired = true;
    }
    public void recordLoginSuccess() { failedLoginAttempts = 0; lastLoginAt = Instant.now(); }
    public void recordLoginFailure(int maxAttempts) {
        failedLoginAttempts++;
        if (failedLoginAttempts >= maxAttempts) accountNonLocked = false;
    }
    public void lock() { accountNonLocked = false; }
    public void unlock() { accountNonLocked = true; failedLoginAttempts = 0; }
}
