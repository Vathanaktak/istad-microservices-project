package kh.edu.istad.platform.business.domain.dto;

public record InitiateBusinessCommand(
        String legalName,
        String displayName,
        String email,
        String phoneNumber
) {
}
