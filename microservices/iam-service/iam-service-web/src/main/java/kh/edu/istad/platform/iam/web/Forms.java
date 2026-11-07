package kh.edu.istad.platform.iam.web;

import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.validation.BindingResult;

/** Form-backing records. Constraint messages are i18n keys resolved in the templates. */
public final class Forms {
    private Forms() {}
    static final String PW = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$";

    public record RegisterForm(
        @NotBlank(message = "validation.required") @Size(min = 3, max = 50, message = "validation.size") @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "validation.username") String username,
        @NotBlank(message = "validation.required") @Email(message = "validation.email") String email,
        @NotBlank(message = "validation.required") @Pattern(regexp = PW, message = "validation.password") String password,
        @NotBlank(message = "validation.required") String confirmPassword,
        @NotBlank(message = "validation.required") String givenName,
        @NotBlank(message = "validation.required") String familyName) {
        @AssertTrue(message = "validation.passwordMismatch")
        public boolean isConfirmPasswordValid() { return password != null && password.equals(confirmPassword); }
    }
    public record ProfileForm(
        @NotBlank(message = "validation.required") String givenName,
        @NotBlank(message = "validation.required") String familyName,
        String nickname,
        @Pattern(regexp = "^(\\+[1-9]\\d{7,14})?$", message = "validation.phone") String phoneNumber,
        @Pattern(regexp = "^(en|km)$", message = "validation.required") String locale) {}
    public record PasswordForm(
        String currentPassword,
        @NotBlank(message = "validation.required") @Pattern(regexp = PW, message = "validation.password") String newPassword,
        @NotBlank(message = "validation.required") String confirmPassword) {
        @AssertTrue(message = "validation.passwordMismatch")
        public boolean isConfirmPasswordValid() { return newPassword != null && newPassword.equals(confirmPassword); }
    }

    /** field -> message key. */
    public static Map<String, String> errors(BindingResult br) {
        Map<String, String> m = new HashMap<>();
        br.getFieldErrors().forEach(e -> m.putIfAbsent("confirmPasswordValid".equals(e.getField()) ? "_global" : e.getField(), String.valueOf(e.getDefaultMessage())));
        br.getGlobalErrors().forEach(e -> m.putIfAbsent("_global", String.valueOf(e.getDefaultMessage())));
        return m;
    }
}
