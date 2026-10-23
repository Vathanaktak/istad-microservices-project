package kh.edu.istad.platform.business.restapi.dto;

import java.util.UUID;

public record GetBusinessResponse(
        UUID businessId,
        String legalName,
        String displayName,
        String email,
        String phoneNumber,
        Boolean active
) {
}
