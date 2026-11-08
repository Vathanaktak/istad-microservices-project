package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import kh.edu.istad.platform.iam.domain.valueobject.Address;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class UpdateUserProfileUseCase {
    private final UserAssembler assembler;
    private final UserRepository users;
    private final IamDomainService iamDomainService;

    public UserResult execute(UpdateUserProfileCommand c) {
        User u = assembler.require(c.userUuid());
        if (c.phoneNumber() != null && !c.phoneNumber().equals(u.getPhoneNumber())) {
            if (users.existsByPhoneNumber(c.phoneNumber())) throw new ConflictException("phone-taken", "Phone number already registered");
            u.setPhoneNumber(c.phoneNumber());
            u.setPhoneNumberVerified(false);
        }
        AddressCommand a = c.address();
        if (a != null) {
            Address current = u.getAddress();
            u.setAddress(new Address(
                a.streetAddress() != null ? a.streetAddress() : (current != null ? current.streetAddress() : null),
                a.locality()      != null ? a.locality()      : (current != null ? current.locality()      : null),
                a.region()        != null ? a.region()        : (current != null ? current.region()        : null),
                a.postalCode()    != null ? a.postalCode()    : (current != null ? current.postalCode()    : null),
                a.country()       != null ? a.country()       : (current != null ? current.country()       : null)
            ));
        }

        // Domain service validates and applies profile field updates
        iamDomainService.updateUser(u,
                c.givenName(), c.familyName(), c.middleName(), c.nickname(),
                c.gender(), c.birthdate(), c.locale(),
                c.profileUrl(), c.pictureUrl(), c.websiteUrl(), c.zoneinfo());

        return assembler.result(users.save(u));
    }
}
