package kh.edu.istad.iam.mapper;

import kh.edu.istad.iam.domain.User;
import kh.edu.istad.iam.dto.UserRequest;
import kh.edu.istad.iam.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRequest userRequest);

    @Mapping(target = "permissionIds", source = "permissions")
    @Mapping(target = "roleIds", source = "roles")
    UserResponse toResponse(User user);

}
