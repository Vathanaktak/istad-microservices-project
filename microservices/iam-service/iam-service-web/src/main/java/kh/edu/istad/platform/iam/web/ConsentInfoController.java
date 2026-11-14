package kh.edu.istad.platform.iam.web;

import java.util.Arrays;
import java.util.List;
import kh.edu.istad.platform.iam.application.result.ConsentInfoResult;
import kh.edu.istad.platform.iam.application.usecase.GetConsentInfoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** JSON feed for the React consent screen (the user already has an IAM browser session at this point). */
@RestController
@RequiredArgsConstructor
public class ConsentInfoController {
    private final GetConsentInfoUseCase consentInfo;

    @GetMapping("/consent-info")
    ConsentInfoResult info(@RequestParam("client_id") String clientId, @RequestParam("scope") String scope) {
        List<String> scopes = Arrays.stream(scope.trim().split("\\s+")).filter(s -> !s.isBlank()).toList();
        return consentInfo.execute(clientId, scopes);
    }
}
