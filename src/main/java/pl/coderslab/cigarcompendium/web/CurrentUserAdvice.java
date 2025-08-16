package pl.coderslab.cigarcompendium.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import pl.coderslab.cigarcompendium.repository.UserRepository;

@Component
public class CurrentUserAdvice {

    private final UserRepository users;

    public CurrentUserAdvice(UserRepository users) {
        this.users = users;
    }

    @ModelAttribute
    public void addCurrentUserId(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            users.findByUsername(username).ifPresent(user -> model.addAttribute("currentUserId", user.getId()));
        }
    }
}
