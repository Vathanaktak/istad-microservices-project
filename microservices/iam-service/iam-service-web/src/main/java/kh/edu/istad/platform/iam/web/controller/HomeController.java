package kh.edu.istad.platform.iam.web.controller;
import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.domain.exception.*;
import kh.edu.istad.platform.iam.web.*; import kh.edu.istad.platform.iam.web.Forms.*; import jakarta.validation.Valid; import java.security.Principal; import java.util.*; import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.validation.BindingResult; import org.springframework.web.bind.annotation.*;
@Controller @RequiredArgsConstructor
public class HomeController {
    private final CurrentUser currentUser;

    @GetMapping("/")
    String index(Principal p, Model m) {
        m.addAttribute("auth", currentUser.get(p));
        return "index";
    }
}
