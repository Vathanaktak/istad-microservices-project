package kh.edu.istad.platform.iam.application.result;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*;
import java.time.*; import java.util.*;
public record RoleResult(UUID uuid, String name, String description, boolean system, boolean active,
        List<PermissionResult> permissions, Instant createdAt, Instant updatedAt) {}
