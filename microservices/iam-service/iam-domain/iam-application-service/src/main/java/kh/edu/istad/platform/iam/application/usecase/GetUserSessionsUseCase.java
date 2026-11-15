package kh.edu.istad.platform.iam.application.usecase;

import java.util.List;
import kh.edu.istad.platform.iam.application.port.out.SessionPort;
import kh.edu.istad.platform.iam.application.result.SessionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserSessionsUseCase {
    private final SessionPort sessionPort;

    public List<SessionResult> execute(String principalName, String currentTokenId) {
        return sessionPort.listSessions(principalName, currentTokenId);
    }
}
