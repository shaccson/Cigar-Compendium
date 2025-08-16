package pl.coderslab.cigarcompendium.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.cigarcompendium.model.Cigar;
import pl.coderslab.cigarcompendium.model.Origin;
import pl.coderslab.cigarcompendium.model.Strength;
import pl.coderslab.cigarcompendium.repository.CigarRepository;
import pl.coderslab.cigarcompendium.repository.ReviewRepository;
import pl.coderslab.cigarcompendium.model.Review;

import java.security.Principal;
import java.util.List;

@Controller
public class CigarController {

    private final CigarRepository cigars;
    private final ReviewRepository reviews;

    public CigarController(CigarRepository cigars, ReviewRepository reviews) {
        this.cigars = cigars;
        this.reviews = reviews;
    }

    @GetMapping("/")
    public String home() { return "redirect:/cigars"; }

    @GetMapping("/cigars")
    public String list(@RequestParam(required = false) Origin origin,
                       @RequestParam(required = false) Strength strength,
                       @RequestParam(required = false) String brand,
                       @RequestParam(required = false) String q,
                       Model model) {

        List<Cigar> result = cigars.search(
                (q == null || q.isBlank()) ? null : q,
                origin,
                strength,
                (brand == null || brand.isBlank()) ? null : brand
        );

        model.addAttribute("cigars", result);
        model.addAttribute("allOrigins", cigars.findAllOrigins());
        model.addAttribute("allStrengths", cigars.findAllStrengths());
        model.addAttribute("allBrands", cigars.findAllBrands());
        model.addAttribute("origin", origin);
        model.addAttribute("strength", strength);
        model.addAttribute("brand", brand);
        model.addAttribute("q", q);
        return "cigars/list";
    }

    @GetMapping("/cigar/{id}")
    public String detail(@PathVariable("id") Long id, Model model, Principal principal) {
        Cigar cigar = cigars.findById(id).orElseThrow();
        List<Review> rs = reviews.findByCigarIdOrderByIdDesc(id);
        double avg = rs.isEmpty() ? 0.0 : rs.stream().mapToInt(Review::getRating).average().orElse(0.0);

        String avgFormatted = String.format("%.2f", avg);

        model.addAttribute("cigar", cigar);
        model.addAttribute("reviews", rs);
        model.addAttribute("avg", avg);
        model.addAttribute("avgFormatted", avgFormatted);

        if (principal != null) {
            reviews.findByUserUsernameAndCigarId(principal.getName(), id)
                    .ifPresentOrElse(
                            r -> { model.addAttribute("reviewForm", r); model.addAttribute("hasMyReview", true); },
                            () -> { model.addAttribute("reviewForm", new Review()); model.addAttribute("hasMyReview", false); }
                    );
        } else {
            model.addAttribute("reviewForm", new Review());
            model.addAttribute("hasMyReview", false);
        }

        return "cigars/detail";
    }
}
