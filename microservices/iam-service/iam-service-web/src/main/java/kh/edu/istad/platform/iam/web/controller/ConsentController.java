package kh.edu.istad.platform.iam.web.controller;
import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.domain.exception.*;
import kh.edu.istad.platform.iam.web.*; import kh.edu.istad.platform.iam.web.Forms.*; import jakarta.validation.Valid; import java.security.Principal; import java.util.*; import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.validation.BindingResult; import org.springframework.web.bind.annotation.*;
@Controller @RequiredArgsConstructor
public class ConsentController {
    private final GetConsentInfoUseCase consentInfo;

    @GetMapping("/oauth2/consent")
    String consent(@RequestParam("client_id") String clientId, @RequestParam("scope") String scope,
                   @RequestParam("state") String state, Model m) {
        m.addAttribute("consent", consentInfo.execute(clientId, Arrays.asList(scope.split(" "))));
        m.addAttribute("state", state);
        return "consent";
    }
}
