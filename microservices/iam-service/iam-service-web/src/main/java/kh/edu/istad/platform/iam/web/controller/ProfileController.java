package kh.edu.istad.platform.iam.web.controller;
import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.domain.exception.*;
import kh.edu.istad.platform.iam.web.*; import kh.edu.istad.platform.iam.web.Forms.*; import jakarta.validation.Valid; import java.security.Principal; import java.util.*; import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.validation.BindingResult; import org.springframework.web.bind.annotation.*;
@Controller @RequiredArgsConstructor @RequestMapping("/profile")
public class ProfileController {
    private final CurrentUser currentUser;
    private final GetUserUseCase getUser;
    private final UpdateUserProfileUseCase updateProfile;
    private final ChangePasswordUseCase changePassword;
    private final ListUserIdentitiesUseCase identities;
    private final UnlinkIdentityUseCase unlink;
    private final ListConnectedAppsUseCase connectedApps;
    private final RevokeConnectedAppUseCase revokeApp;
    @Value("${iam.telegram.bot-username:}") private String telegramBot;

    // ---- edit profile
    @GetMapping("/edit")
    String editForm(Principal p, Model m) {
        UserResult u = getUser.execute(currentUser.uuid(p));
        m.addAttribute("form", new ProfileForm(u.givenName(), u.familyName(), u.nickname() == null ? "" : u.nickname(),
            u.phoneNumber() == null ? "" : u.phoneNumber(), u.locale()));
        m.addAttribute("errors", Map.of());
        m.addAttribute("saved", false);
        return "profile-edit";
    }
    @PostMapping("/edit")
    String edit(Principal p, @Valid @ModelAttribute("form") ProfileForm f, BindingResult br, Model m) {
        Map<String, String> errors = br.hasErrors() ? Forms.errors(br) : new HashMap<>();
        boolean saved = false;
        if (!br.hasErrors()) {
            try {
                updateProfile.execute(new UpdateUserProfileCommand(currentUser.uuid(p), f.givenName(), f.familyName(), null, f.nickname(),
                    null, null, f.phoneNumber() == null || f.phoneNumber().isBlank() ? null : f.phoneNumber(), null, null, null, null, f.locale(), null));
                saved = true;
            } catch (ConflictException e) { errors.put("phoneNumber", "error.conflict"); }
        }
        m.addAttribute("errors", errors);
        m.addAttribute("saved", saved);
        return "profile-edit";
    }

    // ---- change password
    @GetMapping("/password")
    String passwordForm(Principal p, Model m) {
        m.addAttribute("form", new PasswordForm("", "", ""));
        m.addAttribute("errors", Map.of());
        m.addAttribute("saved", false);
        m.addAttribute("hasPassword", currentUser.get(p).hasPassword());
        return "change-password";
    }
    @PostMapping("/password")
    String password(Principal p, @Valid @ModelAttribute("form") PasswordForm f, BindingResult br, Model m) {
        Map<String, String> errors = br.hasErrors() ? Forms.errors(br) : new HashMap<>();
        boolean saved = false;
        if (!br.hasErrors()) {
            try {
                changePassword.execute(new ChangePasswordCommand(currentUser.uuid(p), f.currentPassword(), f.newPassword()));
                saved = true;
            } catch (BusinessRuleException e) { errors.put("currentPassword", "error.wrongPassword"); }
        }
        m.addAttribute("errors", errors);
        m.addAttribute("saved", saved);
        m.addAttribute("hasPassword", true);
        return "change-password";
    }

    // ---- linked social accounts
    @GetMapping("/accounts")
    String accounts(Principal p, @RequestParam(required = false) String error, Model m) {
        List<IdentityResult> linked = identities.execute(currentUser.uuid(p));
        m.addAttribute("identities", linked);
        Set<String> have = new HashSet<>();
        linked.forEach(i -> have.add(i.provider().toLowerCase()));
        List<String> available = new ArrayList<>(List.of("google", "github", "facebook"));
        available.removeAll(have);
        m.addAttribute("available", available);
        m.addAttribute("telegramBot", have.contains("telegram") ? "" : telegramBot);
        m.addAttribute("hasError", error != null);
        return "linked-accounts";
    }
    @PostMapping("/accounts/{identityUuid}/unlink")
    String unlinkAccount(Principal p, @PathVariable UUID identityUuid) {
        try { unlink.execute(currentUser.uuid(p), identityUuid); }
        catch (BusinessRuleException e) { return "redirect:/profile/accounts?error"; }
        return "redirect:/profile/accounts";
    }

    // ---- connected apps (OAuth consents)
    @GetMapping("/apps")
    String apps(Principal p, Model m) {
        m.addAttribute("apps", connectedApps.execute(p.getName()));
        return "connected-apps";
    }
    @PostMapping("/apps/{clientId}/revoke")
    String revoke(Principal p, @PathVariable String clientId) {
        revokeApp.execute(p.getName(), clientId);
        return "redirect:/profile/apps";
    }
}
