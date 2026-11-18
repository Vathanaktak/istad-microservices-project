package kh.edu.istad.platform.iam.application.port.out;

import java.util.List;
import kh.edu.istad.platform.iam.application.result.SessionResult;

public interface SessionPort {
    List<SessionResult> listSessions(String principalName, String currentTokenId);
    void terminateSession(String principalName, String sessionId);
    void terminateAllSessions(String principalName);
}
