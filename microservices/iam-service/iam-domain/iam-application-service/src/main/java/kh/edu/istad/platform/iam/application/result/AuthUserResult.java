package kh.edu.istad.platform.iam.application.result;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*;
import java.time.*; import java.util.*;
/** What the authentication layer needs: profile claims, credentials state and effective authorities. */
public record AuthUserResult(UserResult user, String passwordHash, boolean accountNonExpired, boolean credentialsNonExpired,
        Set<String> roles, Set<String> permissions) {
    public boolean hasPassword() { return passwordHash != null && !passwordHash.isBlank(); }
}
