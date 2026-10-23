package kh.edu.istad.platform.business.domain.dto;

import java.util.UUID;

public record GetBusinessResult(
        UUID businessId,
        String legalName,
        String displayName,
        String email,
        String phoneNumber,
        Boolean active
) {
}
