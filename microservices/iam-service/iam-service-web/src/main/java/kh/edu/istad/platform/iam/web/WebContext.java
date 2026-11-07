package kh.edu.istad.platform.iam.web;

import java.util.Locale;
import org.springframework.context.MessageSource;

/** Per-request view context handed to every JTE template (translations, CSRF, current user). */
public record WebContext(MessageSource messages, Locale locale, String csrfName, String csrfToken, String username) {
    public String t(String key, Object... args) { return messages.getMessage(key, args, key, locale); }
    public String lang() { return locale.getLanguage(); }
    public boolean authenticated() { return username != null; }
}
