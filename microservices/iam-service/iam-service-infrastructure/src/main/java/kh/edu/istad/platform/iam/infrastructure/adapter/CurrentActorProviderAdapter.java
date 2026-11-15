package kh.edu.istad.platform.iam.infrastructure.adapter;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    @Override
    public String currentIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String xf = req.getHeader("X-Forwarded-For");
                if (xf != null && !xf.isBlank()) {
                    return xf.split(",")[0].trim();
                }
                String remote = req.getRemoteAddr();
                if (remote != null && !remote.isBlank()) {
                    return remote;
                }
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}
