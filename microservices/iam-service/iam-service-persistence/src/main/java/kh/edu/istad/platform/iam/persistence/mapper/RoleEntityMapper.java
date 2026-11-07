package kh.edu.istad.platform.iam.persistence.mapper;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.persistence.entity.*; import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface RoleEntityMapper {
    Role toDomain(RoleEntity entity);

    @IgnoreBaseFields
    void update(Role source, @MappingTarget RoleEntity target);

    default RoleEntity toNew(Role source) {
        RoleEntity e = new RoleEntity();
        e.setUuid(source.getUuid());
        update(source, e);
        return e;
    }
}
