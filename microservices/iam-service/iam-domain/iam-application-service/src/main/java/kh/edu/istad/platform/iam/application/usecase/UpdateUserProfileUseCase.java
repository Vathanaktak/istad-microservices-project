package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class UpdateUserProfileUseCase {
    private final UserAssembler assembler;
    private final UserRepository users;

    public UserResult execute(UpdateUserProfileCommand c) {
        User u = assembler.require(c.userUuid());
        if (c.givenName() != null) u.setGivenName(c.givenName());
        if (c.familyName() != null) u.setFamilyName(c.familyName());
        if (c.middleName() != null) u.setMiddleName(c.middleName());
        if (c.nickname() != null) u.setNickname(c.nickname());
        if (c.gender() != null) u.setGender(c.gender());
        if (c.birthdate() != null) u.setBirthdate(c.birthdate());
        if (c.profileUrl() != null) u.setProfileUrl(c.profileUrl());
        if (c.pictureUrl() != null) u.setPictureUrl(c.pictureUrl());
        if (c.websiteUrl() != null) u.setWebsiteUrl(c.websiteUrl());
        if (c.zoneinfo() != null) u.setZoneinfo(c.zoneinfo());
        if (c.locale() != null) u.setLocale(c.locale());
        if (c.phoneNumber() != null && !c.phoneNumber().equals(u.getPhoneNumber())) {
            if (users.existsByPhoneNumber(c.phoneNumber())) throw new ConflictException("phone-taken", "Phone number already registered");
            u.setPhoneNumber(c.phoneNumber());
            u.setPhoneNumberVerified(false);
        }
        AddressCommand a = c.address();
        if (a != null) {
            if (a.streetAddress() != null) u.setAddressStreetAddress(a.streetAddress());
            if (a.locality() != null) u.setAddressLocality(a.locality());
            if (a.region() != null) u.setAddressRegion(a.region());
            if (a.postalCode() != null) u.setAddressPostalCode(a.postalCode());
            if (a.country() != null) u.setAddressCountry(a.country());
        }
        return assembler.result(users.save(u));
    }
}
