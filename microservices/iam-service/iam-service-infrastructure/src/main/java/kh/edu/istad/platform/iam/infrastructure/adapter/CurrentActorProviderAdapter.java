package kh.edu.istad.platform.iam.infrastructure.adapter;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentActorProviderAdapter implements CurrentActorProvider {
    @Override
    public String currentActor() {
        Authentication a = SecurityContextHolder
                .getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() ||
                a instanceof AnonymousAuthenticationToken)
            return "system";
        if (a.getPrincipal() instanceof Jwt jwt &&
                jwt.getSubject() != null)
            return jwt.getSubject();
        return a.getName();
    }
}
