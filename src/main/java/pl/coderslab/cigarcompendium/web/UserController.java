package pl.coderslab.cigarcompendium.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.coderslab.cigarcompendium.model.User;
import pl.coderslab.cigarcompendium.repository.ReviewRepository;
import pl.coderslab.cigarcompendium.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.coderslab.cigarcompendium.model.AppConfig;
import pl.coderslab.cigarcompendium.repository.AppConfigRepository;

@Controller
public class UserController {

    private final UserRepository users;
    private final ReviewRepository reviews;
    private final BCryptPasswordEncoder encoder;

    private final AppConfigRepository appConfigs;

    public UserController(UserRepository users,
                          ReviewRepository reviews,
                          BCryptPasswordEncoder encoder,
                          AppConfigRepository appConfigs) {
        this.users = users;
        this.reviews = reviews;
        this.encoder = encoder;
        this.appConfigs = appConfigs;
    }

    @ModelAttribute("setupOpen")
    public boolean setupOpenAttr() {
        return appConfigs.findById(1L).map(AppConfig::isSetupOpen).orElse(true);
    }

    @GetMapping("/profile")
    public String myProfile(Authentication auth, Model model) {
        User me = users.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("user", me);
        model.addAttribute("userReviews", reviews.findByUserIdOrderByIdDesc(me.getId()));

        if (!model.containsAttribute("emailForm")) model.addAttribute("emailForm", new ChangeEmailForm());
        if (!model.containsAttribute("passwordForm")) model.addAttribute("passwordForm", new ChangePasswordForm());
        if (!model.containsAttribute("deleteForm")) model.addAttribute("deleteForm", new DeleteAccountForm());
        return "user/profile";
    }

    @GetMapping("/user/{id}")
    public String publicProfile(@PathVariable Long id, Model model) {
        var u = users.findById(id).orElseThrow();
        model.addAttribute("user", u);
        model.addAttribute("userReviews", reviews.findByUserIdOrderByIdDesc(id));
        model.addAttribute("publicView", true);
        return "user/profile";
    }

    @PostMapping("/profile/email")
    public String changeEmail(@Valid @ModelAttribute("emailForm") ChangeEmailForm form,
                              BindingResult br,
                              Authentication auth,
                              RedirectAttributes ra) {
        User me = users.findByUsername(auth.getName()).orElseThrow();

        if (!encoder.matches(form.getCurrentPassword(), me.getPassword())) {
            br.rejectValue("currentPassword", "currentPassword.invalid", "Current password is incorrect");
        }
        users.findByEmail(form.getNewEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(me.getId())) {
                br.rejectValue("newEmail", "email.exists", "This email is already in use");
            }
        });
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.emailForm", br);
            ra.addFlashAttribute("emailForm", form);
            ra.addFlashAttribute("error", "Please fix the errors below and submit again.");
            return "redirect:/profile";
        }

        me.setEmail(form.getNewEmail());
        users.save(me);
        ra.addFlashAttribute("success", "Email updated.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") ChangePasswordForm form,
                                 BindingResult br,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        User me = users.findByUsername(auth.getName()).orElseThrow();

        if (!encoder.matches(form.getCurrentPassword(), me.getPassword())) {
            br.rejectValue("currentPassword", "currentPassword.invalid", "Current password is incorrect");
        }
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.passwordForm", br);
            ra.addFlashAttribute("passwordForm", form);
            ra.addFlashAttribute("error", "Please fix the errors below and submit again.");
            return "redirect:/profile";
        }

        me.setPassword(encoder.encode(form.getNewPassword()));
        users.save(me);
        ra.addFlashAttribute("success", "Password changed.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/delete")
    @Transactional
    public String deleteAccount(@Valid @ModelAttribute("deleteForm") DeleteAccountForm form,
                                BindingResult br,
                                Authentication auth,
                                RedirectAttributes ra,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        User me = users.findByUsername(auth.getName()).orElseThrow();

        if (!encoder.matches(form.getCurrentPassword(), me.getPassword())) {
            br.rejectValue("currentPassword", "currentPassword.invalid", "Current password is incorrect");
        }
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.deleteForm", br);
            ra.addFlashAttribute("deleteForm", form);
            ra.addFlashAttribute("error", "Please fix the errors below and submit again.");
            return "redirect:/profile";
        }

        reviews.deleteByUserId(me.getId());
        users.deleteById(me.getId());

        request.getSession(false);
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();

        ra.addFlashAttribute("info", "Your account has been deleted.");
        return "redirect:/login";
    }

    @Data
    public static class ChangeEmailForm {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 120)
        private String newEmail;

        @NotBlank(message = "Current password is required")
        private String currentPassword;
    }

    @Data
    public static class ChangePasswordForm {
        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 100, message = "Password must be 6–100 characters")
        private String newPassword;

        @NotBlank(message = "Current password is required")
        private String currentPassword;
    }

    @Data
    public static class DeleteAccountForm {
        @NotBlank(message = "Current password is required")
        private String currentPassword;
    }
}
