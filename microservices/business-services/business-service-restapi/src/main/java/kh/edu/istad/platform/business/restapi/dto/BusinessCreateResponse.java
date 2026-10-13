package kh.edu.istad.platform.business.restapi.dto;

import java.util.UUID;

public record BusinessCreateResponse(
        UUID businessId,
        String displayName,
        boolean active,
        String message
) {
}
