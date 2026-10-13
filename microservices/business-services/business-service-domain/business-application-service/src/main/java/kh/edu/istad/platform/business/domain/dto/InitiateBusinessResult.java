package kh.edu.istad.platform.business.domain.dto;

import java.util.UUID;

public record InitiateBusinessResult(
        UUID businessId,
        String displayName,
        boolean active,
        String message
) {
}
