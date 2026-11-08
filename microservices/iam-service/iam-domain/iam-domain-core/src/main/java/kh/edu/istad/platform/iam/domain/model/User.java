package kh.edu.istad.platform.iam.domain.model;

import kh.edu.istad.platform.iam.domain.exception.BusinessRuleException;
import kh.edu.istad.platform.iam.domain.valueobject.Address;
import kh.edu.istad.platform.iam.domain.valueobject.Email;
import kh.edu.istad.platform.iam.domain.valueobject.PersonName;
import kh.edu.istad.platform.iam.domain.valueobject.PhoneNumber;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User extends Auditable {
    private Username username;
    private Email email;
    private boolean emailVerified;
    private String passwordHash;
    private PersonName name;
    private String gender;
    private LocalDate birthdate;
    private String profileUrl;
    private String pictureUrl;
    private String websiteUrl;
    private String zoneinfo;
    private String locale = "en";
    private PhoneNumber phoneNumber;
    private boolean phoneNumberVerified;
    private Address address;
    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private int failedLoginAttempts;
    private Instant lastLoginAt;
    private Instant passwordChangedAt;

    // --- convenience accessors kept for backward compat with persistence layer ---
    public String getUsername()    { return username  == null ? null : username.value(); }
    public String getEmail()       { return email     == null ? null : email.value(); }
    public String getGivenName()   { return name      == null ? null : name.givenName(); }
    public String getFamilyName()  { return name      == null ? null : name.familyName(); }
    public String getMiddleName()  { return name      == null ? null : name.middleName(); }
    public String getNickname()    { return name      == null ? null : name.nickname(); }
    public String getPhoneNumber() { return phoneNumber == null ? null : phoneNumber.value(); }
    public String getAddressStreetAddress() { return address == null ? null : address.streetAddress(); }
    public String getAddressLocality()      { return address == null ? null : address.locality(); }
    public String getAddressRegion()        { return address == null ? null : address.region(); }
    public String getAddressPostalCode()    { return address == null ? null : address.postalCode(); }
    public String getAddressCountry()       { return address == null ? null : address.country(); }

    // --- string-based setters (used by MapStruct toDomain from UserEntity) ---
    public void setUsername(String v)    { this.username    = Username.of(v); }
    public void setEmail(String v)       { this.email       = Email.of(v); }
    public void setPhoneNumber(String v) { this.phoneNumber = PhoneNumber.of(v); }
    public void setGivenName(String v)   { rebuildName(v, getFamilyName(), getMiddleName(), getNickname()); }
    public void setFamilyName(String v)  { rebuildName(getGivenName(), v, getMiddleName(), getNickname()); }
    public void setMiddleName(String v)  { rebuildName(getGivenName(), getFamilyName(), v, getNickname()); }
    public void setNickname(String v)    { rebuildName(getGivenName(), getFamilyName(), getMiddleName(), v); }
    public void setAddressStreetAddress(String v) { rebuildAddress(v, getAddressLocality(), getAddressRegion(), getAddressPostalCode(), getAddressCountry()); }
    public void setAddressLocality(String v)      { rebuildAddress(getAddressStreetAddress(), v, getAddressRegion(), getAddressPostalCode(), getAddressCountry()); }
    public void setAddressRegion(String v)        { rebuildAddress(getAddressStreetAddress(), getAddressLocality(), v, getAddressPostalCode(), getAddressCountry()); }
    public void setAddressPostalCode(String v)    { rebuildAddress(getAddressStreetAddress(), getAddressLocality(), getAddressRegion(), v, getAddressCountry()); }
    public void setAddressCountry(String v)       { rebuildAddress(getAddressStreetAddress(), getAddressLocality(), getAddressRegion(), getAddressPostalCode(), v); }

    private void rebuildName(String given, String family, String middle, String nick) {
        this.name = new PersonName(given, family, middle, nick);
    }
    private void rebuildAddress(String street, String locality, String region, String postal, String country) {
        this.address = new Address(street, locality, region, postal, country);
    }

    public String fullName() { return name == null ? "" : name.fullName(); }
    public boolean hasPassword() { return passwordHash != null && !passwordHash.isBlank(); }

    // -------------------------------------------------------------------------
    // Domain Critical Logic
    // -------------------------------------------------------------------------

    /**
     * Validates and initialises a brand-new user account.
     * Must be called before persisting the first time.
     * Mirrors the pattern used in Customer.initiateCustomer().
     */
    public void initiateUser() {
        validateNewUser();
        this.enabled = true;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.failedLoginAttempts = 0;
    }

    private void validateNewUser() {
        if (getId() != null) {
            throw new BusinessRuleException("user-id-not-null", "User id must be null before creation");
        }
        if (username == null || username.value() == null || username.value().isBlank()) {
            throw new BusinessRuleException("user-username-required", "Username must not be blank");
        }
        if (email == null || email.value() == null || email.value().isBlank()) {
            throw new BusinessRuleException("user-email-required", "Email must not be blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new BusinessRuleException("user-password-required", "Password hash must not be blank");
        }
    }

    /**
     * Validates and applies a profile update.
     * At least one of givenName or familyName must be non-blank.
     */
    public void updateProfile(String givenName, String familyName, String middleName, String nickname,
                              String gender, LocalDate birthdate, String locale,
                              String profileUrl, String pictureUrl, String websiteUrl, String zoneinfo) {
        if ((givenName == null || givenName.isBlank()) && (familyName == null || familyName.isBlank())) {
            throw new BusinessRuleException("user-name-required", "At least givenName or familyName must be provided");
        }
        rebuildName(givenName, familyName, middleName, nickname);
        if (gender != null)      this.gender = gender;
        if (birthdate != null)   this.birthdate = birthdate;
        if (locale != null && !locale.isBlank()) this.locale = locale;
        if (profileUrl != null)  this.profileUrl = profileUrl;
        if (pictureUrl != null)  this.pictureUrl = pictureUrl;
        if (websiteUrl != null)  this.websiteUrl = websiteUrl;
        if (zoneinfo != null)    this.zoneinfo = zoneinfo;
    }

    /**
     * Validates and sets a new password hash.
     */
    public void changePassword(String hash) {
        if (hash == null || hash.isBlank()) {
            throw new BusinessRuleException("user-password-blank", "Password hash must not be blank");
        }
        this.passwordHash = hash;
        this.passwordChangedAt = Instant.now();
        this.credentialsNonExpired = true;
    }

    /**
     * Locks the account explicitly (admin action).
     * Throws if already locked.
     */
    public void lockAccount() {
        if (!accountNonLocked) {
            throw new BusinessRuleException("user-already-locked", "User account is already locked");
        }
        accountNonLocked = false;
    }

    /**
     * Unlocks a previously locked account.
     * Throws if the account is not currently locked.
     */
    public void unlockAccount() {
        if (accountNonLocked) {
            throw new BusinessRuleException("user-not-locked", "User account is not locked");
        }
        accountNonLocked = true;
        failedLoginAttempts = 0;
    }

    /**
     * Disables the user account.
     * Throws if already disabled.
     */
    public void disableUser() {
        if (!enabled) {
            throw new BusinessRuleException("user-already-disabled", "User account is already disabled");
        }
        this.enabled = false;
    }

    /**
     * Re-enables a disabled user account.
     * Throws if already enabled.
     */
    public void enableUser() {
        if (enabled) {
            throw new BusinessRuleException("user-already-enabled", "User account is already enabled");
        }
        this.enabled = true;
    }

    /**
     * Soft-deletes the user. Throws if already deleted.
     */
    public void deleteUser(String actor) {
        if (isDeleted()) {
            throw new BusinessRuleException("user-already-deleted", "User account is already deleted");
        }
        softDelete(actor);
    }

    public void recordLoginSuccess() { failedLoginAttempts = 0; lastLoginAt = Instant.now(); }

    public void recordLoginFailure(int maxAttempts) {
        failedLoginAttempts++;
        if (failedLoginAttempts >= maxAttempts) accountNonLocked = false;
    }

    /** @deprecated use {@link #lockAccount()} for explicit admin locking */
    @Deprecated
    public void lock()   { accountNonLocked = false; }

    /** @deprecated use {@link #unlockAccount()} for explicit admin unlocking */
    @Deprecated
    public void unlock() { accountNonLocked = true; failedLoginAttempts = 0; }
}
