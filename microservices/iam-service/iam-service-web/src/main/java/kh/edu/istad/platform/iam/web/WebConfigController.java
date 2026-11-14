package kh.edu.istad.platform.iam.web;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public runtime settings the SPA needs before sign-in (e.g. whether the Telegram login widget is available). */
@RestController
public class WebConfigController {
    @Value("${iam.telegram.bot-username:}") private String telegramBot;

    @GetMapping("/web-config")
    Map<String, Object> config() {
        return Map.of("telegramBot", telegramBot == null ? "" : telegramBot);
    }
}
