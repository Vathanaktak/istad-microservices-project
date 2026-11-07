package kh.edu.istad.platform.iam.persistence.mapper;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.persistence.entity.*; import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface IdentityEntityMapper {
    Identity toDomain(IdentityEntity entity);

    @IgnoreBaseFields
    void update(Identity source, @MappingTarget IdentityEntity target);

    default IdentityEntity toNew(Identity source) {
        IdentityEntity e = new IdentityEntity();
        e.setUuid(source.getUuid());
        update(source, e);
        return e;
    }
}
