package pl.coderslab.cigarcompendium.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.coderslab.cigarcompendium.model.Cigar;
import pl.coderslab.cigarcompendium.model.Origin;
import pl.coderslab.cigarcompendium.model.Strength;
import pl.coderslab.cigarcompendium.repository.CigarRepository;
import pl.coderslab.cigarcompendium.repository.AppConfigRepository;
import pl.coderslab.cigarcompendium.repository.ReviewRepository;
import pl.coderslab.cigarcompendium.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AppConfigRepository configRepo;
    private final CigarRepository cigars;
    private final UserRepository users;
    private final ReviewRepository reviews;
    private final SessionRegistry sessionRegistry;

    private void expireUserSessions(String username) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            String principalUsername = null;
            if (principal instanceof UserDetails ud) {
                principalUsername = ud.getUsername();
            } else if (principal instanceof String s) {
                principalUsername = s;
            }
            if (username.equals(principalUsername)) {
                for (SessionInformation si : sessionRegistry.getAllSessions(principal, false)) {
                    si.expireNow();
                }
            }
        }
    }

    @GetMapping
    public String panel(Model model) {
        var cfg = configRepo.findById(1L).orElseThrow();
        model.addAttribute("setupOpen", cfg.isSetupOpen());
        return "admin/panel";
    }

    @PostMapping("/finish-setup")
    public String finishSetup() {
        var cfg = configRepo.findById(1L).orElseThrow();
        if (cfg.isSetupOpen()) {
            cfg.setSetupOpen(false);
            configRepo.save(cfg);
        }
        return "redirect:/admin?done";
    }

    /* ---------- CIGARS ---------- */

    @GetMapping("/cigars/new")
    public String newCigar(Model model) {
        model.addAttribute("cigar", new Cigar());
        model.addAttribute("allOrigins", Origin.values());
        model.addAttribute("allStrengths", Strength.values());
        return "admin/cigar_form";
    }

    @PostMapping("/cigars")
    public String createCigar(@Valid @ModelAttribute("cigar") Cigar cigar,
                              BindingResult br,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", "Please fix the errors.");
            return "redirect:/admin/cigars/new";
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String original = imageFile.getOriginalFilename();
                String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf(".")) : "";
                String fileName = System.currentTimeMillis() + ext;

                Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
                if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

                Path target = uploadDir.resolve(fileName);
                imageFile.transferTo(target.toFile());

                cigar.setImagePath("/uploads/" + fileName);
            } catch (IOException e) {
                ra.addFlashAttribute("error", "Image upload failed.");
                return "redirect:/admin/cigars/new";
            }
        }

        cigars.save(cigar);
        ra.addFlashAttribute("success", "Cigar added.");
        return "redirect:/cigars";
    }

    @PostMapping("/cigars/{id}/delete")
    @Transactional
    public String deleteCigar(@PathVariable Long id, RedirectAttributes ra) {
        var cigar = cigars.findById(id).orElseThrow();
        String imagePath = cigar.getImagePath();
        if (imagePath != null && imagePath.startsWith("/uploads/")) {
            try {
                Path file = Paths.get("uploads").resolve(Path.of(imagePath).getFileName());
                Files.deleteIfExists(file);
            } catch (IOException e) {
            }
        }
        reviews.deleteByCigarId(id);
        cigars.delete(cigar);

        ra.addFlashAttribute("success", "Cigar deleted.");
        return "redirect:/cigars";
    }

    /* ---------- USERS ---------- */

    @PostMapping("/make-admin")
    public String makeAdmin(@RequestParam String username, RedirectAttributes ra) {
        String clean = username.trim();
        var opt = users.findByUsername(clean);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "No user with username '" + clean + "' found.");
            return "redirect:/admin";
        }
        var u = opt.get();
        if ("ROLE_ADMIN".equals(u.getRole())) {
            ra.addFlashAttribute("info", "User '" + u.getUsername() + "' is already an ADMIN.");
            return "redirect:/admin";
        }
        u.setRole("ROLE_ADMIN");
        users.save(u);

        // to allow admin tools when admin role was given, invalidate session for relog
        expireUserSessions(clean);

        ra.addFlashAttribute("success", "User '" + u.getUsername() + "' successfully promoted to ADMIN.");
        return "redirect:/admin";
    }

    @PostMapping("/delete-user")
    @Transactional
    public String deleteUser(@RequestParam String username,
                             RedirectAttributes ra,
                             HttpServletRequest request) {
        var userOpt = users.findByUsername(username);
        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("error", "No user with username '" + username + "' found.");
            return "redirect:/admin#users";
        }

        var user = userOpt.get();

        reviews.deleteByUserId(user.getId());
        users.delete(user);
        expireUserSessions(username);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && username.equals(auth.getName())) {
            var session = request.getSession(false);
            if (session != null) session.invalidate();
            ra.addFlashAttribute("info", "Your account has been deleted.");
            return "redirect:/login";
        }

        ra.addFlashAttribute("success", "User '" + username + "' deleted along with their reviews.");
        return "redirect:/admin#users";
    }
    @PostMapping("/revoke-admin")
    public String revokeAdmin(@RequestParam String username,
                              RedirectAttributes ra,
                              HttpServletRequest request) {
        var userOpt = users.findByUsername(username);
        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("error", "No user with username '" + username + "' found.");
            return "redirect:/admin#users";
        }

        var user = userOpt.get();
        if (!"ROLE_ADMIN".equals(user.getRole())) {
            ra.addFlashAttribute("info", "User '" + username + "' is not an admin.");
            return "redirect:/admin#users";
        }

        user.setRole("ROLE_USER");
        users.save(user);

        // to prevent admin usage when admin was revoked, invalidate session for relog
        expireUserSessions(username);

        // same but for same user in case it ever happens
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && username.equals(auth.getName())) {
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
            return "redirect:/login?info=Admin%20access%20revoked.%20Please%20sign%20in%20again.";
        }

        ra.addFlashAttribute("success", "User '" + username + "' is no longer an admin.");
        return "redirect:/admin#users";
    }
}
