package kh.edu.istad.platform.iam.persistence.mapper;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.persistence.entity.*; import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface PermissionEntityMapper {
    Permission toDomain(PermissionEntity entity);

    @IgnoreBaseFields
    void update(Permission source, @MappingTarget PermissionEntity target);

    default PermissionEntity toNew(Permission source) {
        PermissionEntity e = new PermissionEntity();
        e.setUuid(source.getUuid());
        update(source, e);
        return e;
    }
}
