package kh.edu.istad.platform.iam.application.usecase;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*; import kh.edu.istad.platform.iam.domain.service.*;
import java.time.*; import java.util.*; import java.util.stream.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class FindOrProvisionFederatedUserUseCase {
    private final UserRepository users;
    private final UserCreator creator;
    private final UserAssembler assembler;

    public AuthUserResult execute(FederatedIdentityCommand c) {
        Optional<Identity> existing = users.findIdentity(c.provider(), c.providerSubject());
        if (existing.isPresent()) {
            Identity i = existing.get();
            refresh(i, c);
            users.saveIdentity(i);
            User u = users.findById(i.getUserId()).orElseThrow(() -> NotFoundException.of("User", i.getUserId()));
            return assembler.authUser(u);
        }
        User user = null;
        // Only auto-link by e-mail when the provider verified it (prevents account takeover).
        if (c.email() != null && c.emailVerified()) user = users.findByUsernameOrEmail(c.email().toLowerCase(Locale.ROOT)).orElse(null);
        if (user == null) user = provision(c);

        Identity i = new Identity();
        i.setUserId(user.getId());
        i.setProvider(c.provider());
        i.setProviderSubject(c.providerSubject());
        refresh(i, c);
        users.saveIdentity(i);
        return assembler.authUser(user);
    }

    private User provision(FederatedIdentityCommand c) {
        String email = c.email() != null ? c.email()
            : c.provider().name().toLowerCase(Locale.ROOT) + "." + c.providerSubject() + "@noreply.iam.local";
        String display = c.displayName() != null ? c.displayName() : (c.username() != null ? c.username() : "User");
        String given = c.givenName(), family = c.familyName();
        if (given == null) {
            String[] parts = display.trim().split("\\s+", 2);
            given = parts[0];
            family = parts.length > 1 ? parts[1] : "-";
        }
        if (family == null) family = "-";
        return creator.create(new NewUser(uniqueUsername(c, email), email, c.email() != null && c.emailVerified(), null,
            given, family, null, null, null, null, c.locale() == null ? "en" : c.locale(), null, c.pictureUrl(), null));
    }

    private String uniqueUsername(FederatedIdentityCommand c, String email) {
        String base = c.username() != null ? c.username() : email.substring(0, email.indexOf('@'));
        base = base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
        if (base.length() < 3) base = c.provider().name().toLowerCase(Locale.ROOT) + base;
        if (base.length() > 40) base = base.substring(0, 40);
        String candidate = base;
        while (users.existsByUsername(candidate)) candidate = base + (1000 + new Random().nextInt(9000));
        return candidate;
    }

    private void refresh(Identity i, FederatedIdentityCommand c) {
        i.setEmail(c.email()); i.setEmailVerified(c.emailVerified()); i.setDisplayName(c.displayName());
        i.setUsername(c.username()); i.setPictureUrl(c.pictureUrl()); i.setAttributes(c.attributes());
        i.recordLogin();
    }
}
