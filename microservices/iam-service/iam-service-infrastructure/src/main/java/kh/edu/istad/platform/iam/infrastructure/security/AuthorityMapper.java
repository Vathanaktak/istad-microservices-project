package kh.edu.istad.platform.iam.infrastructure.security;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class AuthorityMapper {
    private AuthorityMapper() {}
    /** ROLE_ + role name, and the permission name (resource:action). */
    public static List<GrantedAuthority> authorities(AuthUserResult u) {
        List<GrantedAuthority> out = new ArrayList<>();
        u.roles().forEach(r -> out.add(new SimpleGrantedAuthority("ROLE_" + r)));
        u.permissions().forEach(p -> out.add(new SimpleGrantedAuthority(p)));
        return out;
    }
}
