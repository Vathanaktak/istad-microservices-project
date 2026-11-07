package kh.edu.istad.platform.iam.persistence.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*; import java.util.*; import org.hibernate.annotations.*;
@Entity @Table(name = "users") @SQLRestriction("is_deleted = false")
@Getter @Setter
public class UserEntity extends BaseEntity {
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
    private String locale;
    private String phoneNumber;
    private boolean phoneNumberVerified;
    private String addressStreetAddress;
    private String addressLocality;
    private String addressRegion;
    private String addressPostalCode;
    @Column(length = 2) @JdbcTypeCode(java.sql.Types.CHAR)
    private String addressCountry;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private int failedLoginAttempts;
    private Instant lastLoginAt;
    private Instant passwordChangedAt;
}
