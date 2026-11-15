package kh.edu.istad.platform.iam.rest.controller;

import java.time.Instant;
import kh.edu.istad.platform.iam.application.usecase.GetAuditLogsUseCase;
import kh.edu.istad.platform.iam.application.usecase.GetLoginHistoryUseCase;
import kh.edu.istad.platform.iam.rest.dto.Common.PageResponse;
import kh.edu.istad.platform.iam.rest.dto.MonitoringDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MonitoringController {
    private final GetLoginHistoryUseCase loginHistory;
    private final GetAuditLogsUseCase auditLogs;

    @GetMapping("/login-history")
    @PreAuthorize("hasAuthority('audit:read') or hasRole('ADMIN')")
    public PageResponse<LoginRecordResponse> loginHistory(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(required = false) Boolean suspicious,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort) {
        var res = loginHistory.executeGlobal(q, status, suspicious, page, size, sort);
        var mapped = res.map(r -> new LoginRecordResponse(
            r.getId(), r.getUsername(), r.getStatus(), r.getIpAddress(), r.getLocation(),
            r.getUserAgent(), r.getDevice(), r.getBrowser(), r.getOs(), r.isSuspicious(), r.getFailureReason(), r.getOccurredAt()));
        return PageResponse.of(mapped);
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('audit:read') or hasRole('ADMIN')")
    public PageResponse<AuditLogResponse> auditLogs(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String outcome,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort) {
        var res = auditLogs.execute(q, category, outcome, from, to, page, size, sort);
        var mapped = res.map(a -> new AuditLogResponse(
            a.getId(), a.getCategory(), a.getAction(), a.getOutcome(), a.getActor(),
            a.getTarget(), a.getIpAddress(), a.getDetail(), a.getOccurredAt()));
        return PageResponse.of(mapped);
    }
}
