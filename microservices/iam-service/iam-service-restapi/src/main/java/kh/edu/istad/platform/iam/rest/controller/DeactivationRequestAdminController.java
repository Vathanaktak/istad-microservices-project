package kh.edu.istad.platform.iam.rest.controller;

import java.util.Map;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.result.DeactivationProposalResult;
import kh.edu.istad.platform.iam.application.usecase.ApproveDeactivationRequestUseCase;
import kh.edu.istad.platform.iam.application.usecase.ListDeactivationRequestsUseCase;
import kh.edu.istad.platform.iam.application.usecase.RejectDeactivationRequestUseCase;
import kh.edu.istad.platform.iam.rest.dto.ApiMapper;
import kh.edu.istad.platform.iam.rest.dto.Common.PageResponse;
import kh.edu.istad.platform.iam.rest.dto.MonitoringDtos.DeactivationProposalResponse;
import kh.edu.istad.platform.iam.rest.dto.MonitoringDtos.DeactivationReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deactivation-requests")
@RequiredArgsConstructor
public class DeactivationRequestAdminController {
    private final ListDeactivationRequestsUseCase listUseCase;
    private final ApproveDeactivationRequestUseCase approveUseCase;
    private final RejectDeactivationRequestUseCase rejectUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public PageResponse<DeactivationProposalResponse> list(
            @RequestParam(required = false, defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(listUseCase.execute(status, page, size).map(ApiMapper::deactivationProposal));
    }

    @GetMapping("/count-pending")
    @PreAuthorize("hasAuthority('user:read')")
    public Map<String, Long> countPending() {
        return Map.of("count", listUseCase.countPending());
    }

    @PostMapping("/{uuid}/approve")
    @PreAuthorize("hasAuthority('user:update')")
    public DeactivationProposalResponse approve(
            @PathVariable UUID uuid,
            @RequestBody(required = false) DeactivationReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String actor = jwt != null ? (jwt.getClaimAsString("preferred_username") != null ?
                jwt.getClaimAsString("preferred_username") : jwt.getSubject()) : "admin";
        String notes = request != null ? request.reviewNotes() : null;
        DeactivationProposalResult result = approveUseCase.execute(uuid, notes, actor);
        return ApiMapper.deactivationProposal(result);
    }

    @PostMapping("/{uuid}/reject")
    @PreAuthorize("hasAuthority('user:update')")
    public DeactivationProposalResponse reject(
            @PathVariable UUID uuid,
            @RequestBody(required = false) DeactivationReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String actor = jwt != null ? (jwt.getClaimAsString("preferred_username") != null ?
                jwt.getClaimAsString("preferred_username") : jwt.getSubject()) : "admin";
        String notes = request != null ? request.reviewNotes() : null;
        DeactivationProposalResult result = rejectUseCase.execute(uuid, notes, actor);
        return ApiMapper.deactivationProposal(result);
    }
}
