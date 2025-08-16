package pl.coderslab.cigarcompendium.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.coderslab.cigarcompendium.model.AppConfig;
import pl.coderslab.cigarcompendium.model.User;
import pl.coderslab.cigarcompendium.repository.AppConfigRepository;
import pl.coderslab.cigarcompendium.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository users;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AppConfigRepository appConfigs;

    @ModelAttribute("setupOpen")
    public boolean setupOpen() {
        return appConfigs.findById(1L).map(AppConfig::isSetupOpen).orElse(true);
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "registered", required = false) String registered,
                        Model model) {
        if (registered != null) {
            model.addAttribute("info", "Registration successful. You can sign in now.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           Model model,
                           RedirectAttributes ra) {

        if (user.getUsername() != null && !user.getUsername().isBlank()
                && users.findByUsername(user.getUsername()).isPresent()) {
            result.rejectValue("username", "user.username.exists", "This username is taken");
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()
                && users.findByEmail(user.getEmail()).isPresent()) {
            result.rejectValue("email", "user.email.exists", "This email is already registered");
        }

        if (result.hasErrors()) {
            model.addAttribute("formError", "Please fix the errors below and submit again.");
            return "auth/register";
        }

        boolean isSetupOpen = setupOpen();
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole(isSetupOpen ? "ROLE_ADMIN" : "ROLE_USER");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        users.save(user);

        return "redirect:/login?registered=1";
    }
}
