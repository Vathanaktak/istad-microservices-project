package kh.edu.istad.platform.iam.infrastructure.oidc;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

/** sub = users.uuid; access token gets roles + permissions; ID token gets scope-based OIDC claims. */
@Component @RequiredArgsConstructor
public class IamTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    private final LoadUserForAuthenticationUseCase loadUser;

    @Override
    public void customize(JwtEncodingContext ctx) {
        if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(ctx.getAuthorizationGrantType())) return;
        AuthUserResult user = loadUser.execute(ctx.getPrincipal().getName());
        ctx.getClaims().subject(user.user().uuid().toString());
        if (OAuth2TokenType.ACCESS_TOKEN.equals(ctx.getTokenType())) {
            ctx.getClaims().claim("roles", user.roles());
            ctx.getClaims().claim("permissions", user.permissions());
        } else if (ctx.getTokenType() != null && OidcParameterNames.ID_TOKEN.equals(ctx.getTokenType().getValue())) {
            OidcClaimsMapper.claims(user, ctx.getAuthorizedScopes()).forEach((k, v) -> {
                if (!"sub".equals(k)) ctx.getClaims().claim(k, v);
            });
        }
    }
}
