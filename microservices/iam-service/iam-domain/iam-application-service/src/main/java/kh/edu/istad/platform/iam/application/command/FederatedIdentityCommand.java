package kh.edu.istad.platform.iam.application.command;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*;
import java.time.*; import java.util.*;
public record FederatedIdentityCommand(IdentityProvider provider, String providerSubject, String email, boolean emailVerified,
        String displayName, String username, String pictureUrl, String givenName, String familyName, String locale,
        Map<String, Object> attributes) {}
