package kh.edu.istad.platform.iam.web;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "kh.edu.istad.platform.iam.web")
@RequiredArgsConstructor
public class WebAdvice {
    private final MessageSource messages;

    @ModelAttribute("ctx")
    WebContext ctx(HttpServletRequest req, Principal principal, Locale locale) {
        CsrfToken csrf = (CsrfToken) req.getAttribute("_csrf");
        return new WebContext(messages, locale, csrf == null ? "_csrf" : csrf.getParameterName(),
            csrf == null ? "" : csrf.getToken(), principal == null ? null : principal.getName());
    }
}
