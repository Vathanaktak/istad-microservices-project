package kh.edu.istad.platform.iam.persistence.mapper;
import kh.edu.istad.platform.iam.domain.model.*;
import kh.edu.istad.platform.iam.domain.valueobject.*;
import kh.edu.istad.platform.iam.persistence.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    // --- Entity → Domain ---
    // Map flat entity columns → VO fields via string setters on User
    @Mapping(target = "username",    expression = "java(entity.getUsername())")
    @Mapping(target = "email",       expression = "java(entity.getEmail())")
    @Mapping(target = "phoneNumber", expression = "java(entity.getPhoneNumber())")
    @Mapping(target = "name",        expression = "java(new kh.edu.istad.platform.iam.domain.valueobject.PersonName(entity.getGivenName(), entity.getFamilyName(), entity.getMiddleName(), entity.getNickname()))")
    @Mapping(target = "address",     expression = "java(new kh.edu.istad.platform.iam.domain.valueobject.Address(entity.getAddressStreetAddress(), entity.getAddressLocality(), entity.getAddressRegion(), entity.getAddressPostalCode(), entity.getAddressCountry()))")
    User toDomain(UserEntity entity);

    // --- Domain → Entity (update) ---
    // User exposes getters that unwrap the VOs to plain strings, so MapStruct auto-maps them
    @IgnoreBaseFields
    @Mapping(target = "givenName",           source = "givenName")
    @Mapping(target = "familyName",          source = "familyName")
    @Mapping(target = "middleName",          source = "middleName")
    @Mapping(target = "nickname",            source = "nickname")
    @Mapping(target = "phoneNumber",         source = "phoneNumber")
    @Mapping(target = "addressStreetAddress",source = "addressStreetAddress")
    @Mapping(target = "addressLocality",     source = "addressLocality")
    @Mapping(target = "addressRegion",       source = "addressRegion")
    @Mapping(target = "addressPostalCode",   source = "addressPostalCode")
    @Mapping(target = "addressCountry",      source = "addressCountry")
    void update(User source, @MappingTarget UserEntity target);

    default UserEntity toNew(User source) {
        UserEntity e = new UserEntity();
        e.setUuid(source.getUuid());
        update(source, e);
        return e;
    }
}
