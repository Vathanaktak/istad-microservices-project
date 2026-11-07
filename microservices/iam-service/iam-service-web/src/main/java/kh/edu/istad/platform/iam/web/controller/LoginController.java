package kh.edu.istad.platform.iam.web.controller;
import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.domain.exception.*;
import kh.edu.istad.platform.iam.web.*; import kh.edu.istad.platform.iam.web.Forms.*; import jakarta.validation.Valid; import java.security.Principal; import java.util.*; import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.validation.BindingResult; import org.springframework.web.bind.annotation.*;
@Controller
public class LoginController {
    @Value("${iam.telegram.bot-username:}") private String telegramBot;

    @GetMapping("/login")
    String login(@RequestParam(required = false) String error, @RequestParam(required = false) String logout, Model m) {
        m.addAttribute("telegramBot", telegramBot);
        m.addAttribute("hasError", error != null);
        m.addAttribute("loggedOut", logout != null);
        return "login";
    }
}
