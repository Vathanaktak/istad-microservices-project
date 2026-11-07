package kh.edu.istad.platform.iam.infrastructure.security;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/** Adapter: Spring Security UserDetailsService -> LoadUserForAuthenticationUseCase. */
@Service @RequiredArgsConstructor
public class IamUserDetailsService implements UserDetailsService {
    private final LoadUserForAuthenticationUseCase loadUser;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) {
        try {
            AuthUserResult a = loadUser.execute(usernameOrEmail);
            return org.springframework.security.core.userdetails.User.builder()
                .username(a.user().username())
                // social-only users have no password: use a hash that can never match
                .password(a.hasPassword() ? a.passwordHash() : "{bcrypt}!")
                .disabled(!a.user().enabled())
                .accountExpired(!a.accountNonExpired())
                .accountLocked(!a.user().accountNonLocked())
                .credentialsExpired(!a.credentialsNonExpired())
                .authorities(AuthorityMapper.authorities(a))
                .build();
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(usernameOrEmail);
        }
    }
}
