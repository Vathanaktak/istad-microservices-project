package kh.edu.istad.platform.iam.application.support;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
/** Internal input of UserCreator (shared by register, create and federated provisioning). */
public record NewUser(String username, String email, boolean emailVerified, String rawPassword, String givenName, String familyName,
        String gender, LocalDate birthdate, String phoneNumber, String zoneinfo, String locale, AddressCommand address,
        String pictureUrl, Collection<UUID> roleUuids) {}
