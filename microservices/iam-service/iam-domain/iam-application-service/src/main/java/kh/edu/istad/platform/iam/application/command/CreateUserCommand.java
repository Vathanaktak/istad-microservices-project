package kh.edu.istad.platform.iam.application.command;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*;
import java.time.*; import java.util.*;
public record CreateUserCommand(String username, String email, String password, String givenName, String familyName,
        String gender, LocalDate birthdate, String phoneNumber, String zoneinfo, String locale,
        AddressCommand address, List<UUID> roleUuids) {}
