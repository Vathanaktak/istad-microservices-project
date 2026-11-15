package kh.edu.istad.platform.iam.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.DeactivationRequestRepository;
import kh.edu.istad.platform.iam.application.port.out.SessionPort;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.result.DeactivationProposalResult;
import kh.edu.istad.platform.iam.application.usecase.ApproveDeactivationRequestUseCase;
import kh.edu.istad.platform.iam.application.usecase.RejectDeactivationRequestUseCase;
import kh.edu.istad.platform.iam.domain.exception.ConflictException;
import kh.edu.istad.platform.iam.domain.model.DeactivationRequest;
import kh.edu.istad.platform.iam.domain.model.User;
import kh.edu.istad.platform.iam.domain.service.IamDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeactivationRequestUseCaseTest {
    private final DeactivationRequestRepository repository = mock(DeactivationRequestRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final IamDomainService iamDomainService = mock(IamDomainService.class);
    private final SessionPort sessionPort = mock(SessionPort.class);
    private final AuditLogRepository auditLogs = mock(AuditLogRepository.class);

    private ApproveDeactivationRequestUseCase approveUseCase;
    private RejectDeactivationRequestUseCase rejectUseCase;

    @BeforeEach
    void setup() {
        approveUseCase = new ApproveDeactivationRequestUseCase(repository, users, iamDomainService, sessionPort, auditLogs);
        rejectUseCase = new RejectDeactivationRequestUseCase(repository, users, auditLogs);
    }

    @Test
    void approveDeactivation_disablesUser_terminatesSessions_marksProcessed() {
        UUID reqUuid = UUID.randomUUID();
        User u = new User();
        u.setId(10L);
        u.setUuid(UUID.randomUUID());
        u.setUsername("john");
        u.setEnabled(true);

        DeactivationRequest req = new DeactivationRequest();
        req.setId(1L);
        req.setUuid(reqUuid);
        req.setUserId(10L);
        req.setStatus("PENDING");
        req.setReason("privacy");

        when(repository.findByUuid(reqUuid)).thenReturn(Optional.of(req));
        when(users.findById(10L)).thenReturn(Optional.of(u));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeactivationProposalResult res = approveUseCase.execute(reqUuid, "Approved by admin", "admin");

        assertThat(res.status()).isEqualTo("PROCESSED");
        assertThat(res.reviewedBy()).isEqualTo("admin");
        assertThat(res.reviewNotes()).isEqualTo("Approved by admin");
        verify(iamDomainService).disableUser(u);
        verify(users).save(u);
        verify(sessionPort).terminateAllSessions("john");
        verify(auditLogs).save(any());
    }

    @Test
    void rejectDeactivation_leavesUserEnabled_marksRejected() {
        UUID reqUuid = UUID.randomUUID();
        User u = new User();
        u.setId(10L);
        u.setUuid(UUID.randomUUID());
        u.setUsername("john");
        u.setEnabled(true);

        DeactivationRequest req = new DeactivationRequest();
        req.setId(1L);
        req.setUuid(reqUuid);
        req.setUserId(10L);
        req.setStatus("PENDING");
        req.setReason("other");

        when(repository.findByUuid(reqUuid)).thenReturn(Optional.of(req));
        when(users.findById(10L)).thenReturn(Optional.of(u));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeactivationProposalResult res = rejectUseCase.execute(reqUuid, "Rejected by policy", "admin");

        assertThat(res.status()).isEqualTo("REJECTED");
        verify(iamDomainService, never()).disableUser(any());
        verify(sessionPort, never()).terminateAllSessions(any());
        verify(auditLogs).save(any());
    }

    @Test
    void approveDeactivation_alreadyProcessed_throwsConflict() {
        UUID reqUuid = UUID.randomUUID();
        DeactivationRequest req = new DeactivationRequest();
        req.setStatus("PROCESSED");

        when(repository.findByUuid(reqUuid)).thenReturn(Optional.of(req));

        assertThatThrownBy(() -> approveUseCase.execute(reqUuid, null, "admin"))
                .isInstanceOf(ConflictException.class);
    }
}
