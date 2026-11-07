package kh.edu.istad.platform.iam.application.port.out;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*;
import java.time.*; import java.util.*;
public interface UserRepository {
    Optional<User> findByUuid(UUID uuid);
    Optional<User> findById(Long id);
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    User save(User user);
    PageResult<User> search(UserFilterQuery query);

    List<UserRole> findUserRoles(Long userId);
    UserRole saveUserRole(UserRole assignment);
    List<UserPermission> findUserPermissions(Long userId);
    UserPermission saveUserPermission(UserPermission assignment);

    Optional<Identity> findIdentity(IdentityProvider provider, String providerSubject);
    List<Identity> findIdentities(Long userId);
    Identity saveIdentity(Identity identity);
}
