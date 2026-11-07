package kh.edu.istad.platform.iam.infrastructure.adapter;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.domain.model.IdentityProvider; import kh.edu.istad.platform.iam.domain.exception.*; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordHasherAdapter implements PasswordHasher {
    private final PasswordEncoder encoder;
    @Override public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    @Override public boolean matches(
            String rawPassword,
            String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
