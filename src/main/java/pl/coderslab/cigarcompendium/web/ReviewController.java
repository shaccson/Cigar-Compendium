package pl.coderslab.cigarcompendium.web;

import pl.coderslab.cigarcompendium.model.Cigar;
import pl.coderslab.cigarcompendium.model.Review;
import pl.coderslab.cigarcompendium.model.User;
import pl.coderslab.cigarcompendium.repository.CigarRepository;
import pl.coderslab.cigarcompendium.repository.ReviewRepository;
import pl.coderslab.cigarcompendium.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviews;
    private final UserRepository users;
    private final CigarRepository cigars;

    @PostMapping("/review/save")
    public String save(@RequestParam Long cigarId,
                       @Valid @ModelAttribute("reviewForm") Review form,
                       BindingResult br,
                       Principal principal,
                       Model model) {
        Cigar cigar = cigars.findById(cigarId).orElseThrow();

        if (principal == null) {
            return "redirect:/login";
        }
        User user = users.findByUsername(principal.getName()).orElseThrow();

        if (br.hasErrors()) {
            List<Review> rs = reviews.findByCigarIdOrderByIdDesc(cigarId);
            model.addAttribute("cigar", cigar);
            model.addAttribute("reviews", rs);
            double avg = rs.isEmpty() ? 0.0 : rs.stream().mapToInt(Review::getRating).average().orElse(0.0);
            model.addAttribute("roundedAvg", Math.round(avg));
            model.addAttribute("hasMyReview", form.getId() != null);
            return "cigars/detail";
        }

        reviews.findByUserUsernameAndCigarId(user.getUsername(), cigarId)
                .ifPresentOrElse(existing -> {
                    existing.setRating(form.getRating());
                    existing.setComment(form.getComment());
                    reviews.save(existing);
                }, () -> {
                    form.setUser(user);
                    form.setCigar(cigar);
                    reviews.save(form);
                });

        return "redirect:/cigar/" + cigarId;
    }

    @PostMapping("/reviews/{id}/delete")
    public String delete(@PathVariable Long id,
                         Authentication auth,
                         HttpServletRequest request,
                         RedirectAttributes ra) {
        var reviewOpt = reviews.findById(id);
        if (reviewOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Review not found.");
            return "redirect:/cigars";
        }
        var review = reviewOpt.get();

        boolean isAdmin = auth != null && auth.getAuthorities()
                .stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isOwner = auth != null && review.getUser() != null
                && review.getUser().getUsername().equals(auth.getName());

        if (!isAdmin && !isOwner) {
            ra.addFlashAttribute("error", "You are not allowed to delete this review.");
            return "redirect:/cigars";
        }

        reviews.delete(review);
        ra.addFlashAttribute("success", "Review deleted.");

        String ref = request.getHeader("Referer");
        if (ref != null && !ref.isBlank()) {
            return "redirect:" + ref;
        }
        if (review.getCigar() != null) {
            return "redirect:/cigar/" + review.getCigar().getId();
        }
        return "redirect:/cigars";
    }
}
