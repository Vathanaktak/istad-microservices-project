package kh.edu.istad.platform.iam.web;

import kh.edu.istad.platform.iam.application.result.AuthUserResult;
import kh.edu.istad.platform.iam.application.usecase.LoadUserForAuthenticationUseCase;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class CurrentUser {
    private final LoadUserForAuthenticationUseCase load;
    public AuthUserResult get(Principal p) { return load.execute(p.getName()); }
    public UUID uuid(Principal p) { return get(p).user().uuid(); }
}
