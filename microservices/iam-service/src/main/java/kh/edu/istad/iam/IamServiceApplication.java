package kh.edu.istad.iam;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller
public class IamServiceApplication {

    @GetMapping("/login")
    public String login(HttpServletRequest request, CsrfToken csrfToken){
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        return "login";
    }

    public static void main(String[] args) {
        SpringApplication.run(IamServiceApplication.class, args);
    }

}
