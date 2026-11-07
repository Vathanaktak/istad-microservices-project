package kh.edu.istad.platform.iam.application.command;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*;
import java.time.*; import java.util.*;
/** Null fields are left unchanged (PATCH semantics). */
public record UpdateUserProfileCommand(UUID userUuid, String givenName, String familyName, String middleName, String nickname,
        String gender, LocalDate birthdate, String phoneNumber, String profileUrl, String pictureUrl, String websiteUrl,
        String zoneinfo, String locale, AddressCommand address) {}
