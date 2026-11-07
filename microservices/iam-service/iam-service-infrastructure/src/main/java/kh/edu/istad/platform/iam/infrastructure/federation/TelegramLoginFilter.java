package kh.edu.istad.platform.iam.infrastructure.federation;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.*;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import kh.edu.istad.platform.iam.infrastructure.security.AuthorityMapper;

/**
 * Telegram Login Widget callback (GET /login/telegram). The payload is verified with
 * HMAC-SHA256 keyed by SHA256(bot token) as documented by Telegram.
 */
public class TelegramLoginFilter extends OncePerRequestFilter {
    private final String botToken;
    private final FindOrProvisionFederatedUserUseCase provision;
    private final LinkIdentityUseCase link;
    private final LoadUserForAuthenticationUseCase load;
    private final HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
    private final SavedRequestAwareAuthenticationSuccessHandler success = new SavedRequestAwareAuthenticationSuccessHandler();

    public TelegramLoginFilter(String botToken, FindOrProvisionFederatedUserUseCase provision, LinkIdentityUseCase link,
                               LoadUserForAuthenticationUseCase load) {
        this.botToken = botToken; this.provision = provision; this.link = link; this.load = load;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest r) { return !"/login/telegram".equals(r.getServletPath()) || !"GET".equals(r.getMethod()); }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException {
        Map<String, String> p = new TreeMap<>();
        req.getParameterMap().forEach((k, v) -> p.put(k, v[0]));
        if (botToken.isBlank() || !valid(p)) { res.sendRedirect("/login?error"); return; }
        try {
            String name = ((p.getOrDefault("first_name", "") + " " + p.getOrDefault("last_name", "")).trim());
            FederatedIdentityCommand cmd = new FederatedIdentityCommand(IdentityProvider.TELEGRAM, p.get("id"), null, false,
                name.isBlank() ? p.get("username") : name, p.get("username"), p.get("photo_url"),
                p.get("first_name"), p.get("last_name"), "en", new HashMap<>(p));
            Authentication current = SecurityContextHolder.getContext().getAuthentication();
            AuthUserResult user;
            if (current != null && current.isAuthenticated() && !(current instanceof AnonymousAuthenticationToken)) {
                link.execute(load.execute(current.getName()).user().uuid(), cmd);
                user = load.execute(current.getName());
            } else user = provision.execute(cmd);
            if (!user.user().enabled() || !user.user().accountNonLocked()) { res.sendRedirect("/login?error"); return; }

            var principal = org.springframework.security.core.userdetails.User.builder()
                .username(user.user().username()).password("{bcrypt}!").authorities(AuthorityMapper.authorities(user)).build();
            Authentication auth = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
            if (req.getSession(false) != null) req.changeSessionId();
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);
            repo.saveContext(ctx, req, res);
            success.onAuthenticationSuccess(req, res, auth);
        } catch (DomainException e) {
            res.sendRedirect("/login?error");
        }
    }

    private boolean valid(Map<String, String> params) {
        String hash = params.get("hash");
        String authDate = params.get("auth_date");
        if (hash == null || authDate == null || !params.containsKey("id")) return false;
        try {
            if (Instant.now().getEpochSecond() - Long.parseLong(authDate) > 86_400) return false;
            String check = params.entrySet().stream().filter(e -> !e.getKey().equals("hash"))
                .map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("\n"));
            byte[] secret = MessageDigest.getInstance("SHA-256").digest(botToken.getBytes(StandardCharsets.UTF_8));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] calc = HexFormat.of().formatHex(mac.doFinal(check.getBytes(StandardCharsets.UTF_8))).getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(calc, hash.toLowerCase().getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException | NumberFormatException e) { return false; }
    }
}
