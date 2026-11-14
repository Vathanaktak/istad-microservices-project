package kh.edu.istad.platform.iam.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the React single-page application (built from /frontend into classpath:/static).
 * Every client-side route is forwarded to index.html so deep links and browser refresh work.
 */
@Controller
public class SpaController {

    @GetMapping({
        "/", "/login", "/register", "/forgot-password", "/reset-password", "/oauth2/consent",
        "/auth/callback", "/account", "/account/**", "/admin", "/admin/**"
    })
    String index() {
        return "forward:/index.html";
    }
}
