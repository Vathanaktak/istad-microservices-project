package kh.edu.istad.platform.iam.web.controller;
import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.domain.exception.*;
import kh.edu.istad.platform.iam.web.*; import kh.edu.istad.platform.iam.web.Forms.*; import jakarta.validation.Valid; import java.security.Principal; import java.util.*; import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.validation.BindingResult; import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterUserUseCase register;

    @GetMapping("/register")
    String form(Model m) {
        m.addAttribute("form", new RegisterForm("", "", "", "", "", ""));
        m.addAttribute("errors", Map.of());
        return "register";
    }

    @PostMapping("/register")
    String submit(@Valid @ModelAttribute("form") RegisterForm form, BindingResult br, Model m, Locale locale) {
        Map<String, String> errors = br.hasErrors() ? Forms.errors(br) : new HashMap<>();
        if (!br.hasErrors()) {
            try {
                register.execute(new RegisterUserCommand(form.username(), form.email(), form.password(), form.givenName(),
                    form.familyName(), "km".equals(locale.getLanguage()) ? "km" : "en"));
                return "redirect:/login?registered";
            } catch (ConflictException e) {
                errors.put(e.getCode().startsWith("email") ? "email" : "username", "error.conflict");
            }
        }
        m.addAttribute("errors", errors);
        return "register";
    }
}
