package kh.edu.istad.platform.iam.bootstrap;

import kh.edu.istad.platform.iam.application.command.CreateUserCommand;
import kh.edu.istad.platform.iam.application.port.out.RoleRepository;
import kh.edu.istad.platform.iam.application.usecase.CreateUserUseCase;
import kh.edu.istad.platform.iam.domain.model.Role;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;

/** Creates the first administrator from environment variables (IAM_ADMIN_*), only if configured and absent. */
@Component @RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private final CreateUserUseCase createUser;
    private final UserRepository users;
    private final RoleRepository roles;
    @Value("${iam.admin.username:}") private String username;
    @Value("${iam.admin.email:}") private String email;
    @Value("${iam.admin.password:}") private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() || email.isBlank() || password.isBlank() || users.existsByUsername(username)) return;
        Role admin = roles.findByName("ADMIN").orElseThrow();
        createUser.execute(new CreateUserCommand(username, email, password, "System", "Administrator", null, null, null,
            "Asia/Phnom_Penh", "en", null, List.of(admin.getUuid())));
        log.info("Seeded administrator '{}'", username);
    }
}
