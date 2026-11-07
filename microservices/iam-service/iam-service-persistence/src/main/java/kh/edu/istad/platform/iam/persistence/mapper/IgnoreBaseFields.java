package kh.edu.istad.platform.iam.persistence.mapper;

import java.lang.annotation.*;
import org.mapstruct.Mapping;

/** Keeps identity, uuid, audit columns and version under the control of JPA/auditing when copying domain -> entity. */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Mapping(target = "id", ignore = true)
@Mapping(target = "uuid", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "createdBy", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
@Mapping(target = "updatedBy", ignore = true)
@Mapping(target = "version", ignore = true)
public @interface IgnoreBaseFields {}
