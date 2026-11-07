package kh.edu.istad.platform.iam.persistence.mapper;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.persistence.entity.*; import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    User toDomain(UserEntity entity);

    @IgnoreBaseFields
    void update(User source, @MappingTarget UserEntity target);

    default UserEntity toNew(User source) {
        UserEntity e = new UserEntity();
        e.setUuid(source.getUuid());
        update(source, e);
        return e;
    }
}
