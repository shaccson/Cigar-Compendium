package pl.coderslab.cigarcompendium.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.coderslab.cigarcompendium.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCigarIdOrderByIdDesc(Long cigarId);
    List<Review> findByUserIdOrderByIdDesc(Long userId);
    Optional<Review> findByUserUsernameAndCigarId(String username, Long cigarId);

    void deleteByUserId(Long userId);
    void deleteByCigarId(Long cigarId);

}
