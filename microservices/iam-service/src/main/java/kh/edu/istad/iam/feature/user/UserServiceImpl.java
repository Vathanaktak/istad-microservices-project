package kh.edu.istad.iam.feature.user;

import kh.edu.istad.iam.domain.Permission;
import kh.edu.istad.iam.domain.Role;
import kh.edu.istad.iam.domain.User;
import kh.edu.istad.iam.dto.UserRequest;
import kh.edu.istad.iam.dto.UserResponse;
import kh.edu.istad.iam.feature.permission.PermissionJpaRepository;
import kh.edu.istad.iam.feature.role.RoleJpaRepository;
import kh.edu.istad.iam.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserJapRepository userJapRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final PermissionJpaRepository permissionJpaRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<UserResponse> createUser(UserRequest userRequest) {

       User user = userMapper.toUser(userRequest);

        Set<Role> roles = new HashSet<>(roleJpaRepository.findAllById(userRequest.roleIds()));

        if(roles.isEmpty()) {
            throw new RuntimeException("Roles not found");
        }

        user.setRoles(roles);
         Set<Permission> permissions = new HashSet<>(permissionJpaRepository
                 .findAllById(userRequest.permissionIds()));
         if(permissions.isEmpty()) {
             throw new RuntimeException("Permissions not found");
         }
         user.setPermissions(permissions);

         var savedUser = userJapRepository.save(user);

         return Optional.of(userMapper.toResponse(savedUser));

    }
}
