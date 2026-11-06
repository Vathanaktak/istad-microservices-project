package kh.edu.istad.iam.init;


import jakarta.annotation.PostConstruct;
import kh.edu.istad.iam.config.PasswordConfig;
import kh.edu.istad.iam.domain.Permission;
import kh.edu.istad.iam.domain.Role;
import kh.edu.istad.iam.domain.User;
import kh.edu.istad.iam.feature.permission.PermissionJpaRepository;
import kh.edu.istad.iam.feature.role.RoleJpaRepository;
import kh.edu.istad.iam.feature.user.UserJapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class InitCore {

    private final PermissionJpaRepository permissionJpaRepository;
    private final UserJapRepository userJapRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final PasswordConfig passwordConfig;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init(){

        Permission permission = new Permission().builder()
                .permission("READ")
                .description("has ha")
                .build();

        Role role = new Role()
                .builder()
                .role("ADMIN")
                .description("He is Admin")
                .build();

        User user = new User()
                .builder()
                .familyName("thai")
                .givenName("vathanak")
                .email("vathanak@gmail.com")
                .gender("Male")
                .username("tak")
                .password(passwordEncoder.encode("takSecret"))
                .emailVerified(true)
                .build();

        if(!userJapRepository.existsByUsername("tak")){

            role = roleJpaRepository.save(role);
            permission = permissionJpaRepository.save(permission);

            user.setRoles(Set.of(role));
            user.setPermissions(Set.of(permission));

            userJapRepository.save(user);

        }
    }

}
