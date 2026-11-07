package kh.edu.istad.platform.iam.infrastructure.federation;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.Component;
import kh.edu.istad.platform.iam.infrastructure.security.AuthorityMapper;

/**
 * Social login adapter. If a user is already signed in the provider account is linked to them
 * (profile > linked accounts), otherwise FindOrProvisionFederatedUserUseCase signs in / creates the user.
 */
@Component @RequiredArgsConstructor
public class FederatedOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final FindOrProvisionFederatedUserUseCase provision;
    private final LinkIdentityUseCase link;
    private final LoadUserForAuthenticationUseCase load;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User raw = delegate.loadUser(request);
        FederatedIdentityCommand cmd = FederatedIdentityMapper.map(request.getClientRegistration().getRegistrationId(), raw.getAttributes());
        try {
            Authentication current = SecurityContextHolder.getContext().getAuthentication();
            AuthUserResult user;
            if (current != null && current.isAuthenticated() && !(current instanceof AnonymousAuthenticationToken)) {
                UUID me = load.execute(current.getName()).user().uuid();
                link.execute(me, cmd);
                user = load.execute(current.getName());
            } else {
                user = provision.execute(cmd);
            }
            if (!user.user().enabled() || !user.user().accountNonLocked() || !user.accountNonExpired())
                throw new OAuth2AuthenticationException(new OAuth2Error("account_unavailable", "Account is disabled or locked", null));
            return new DefaultOAuth2User(AuthorityMapper.authorities(user), Map.of("username", user.user().username()), "username");
        } catch (DomainException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error("federation_failed", e.getMessage(), null), e);
        }
    }
}
