package pl.coderslab.cigarcompendium.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.coderslab.cigarcompendium.model.Cigar;
import pl.coderslab.cigarcompendium.model.Origin;
import pl.coderslab.cigarcompendium.model.Strength;

import java.util.List;

public interface CigarRepository extends JpaRepository<Cigar, Long> {

    @Query("""
        SELECT c FROM Cigar c
        WHERE (:q IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:origin IS NULL OR c.origin = :origin)
          AND (:strength IS NULL OR c.strength = :strength)
          AND (:brand IS NULL OR LOWER(c.brand) = LOWER(:brand))
        ORDER BY c.name
    """)
    List<Cigar> search(@Param("q") String q,
                       @Param("origin") Origin origin,
                       @Param("strength") Strength strength,
                       @Param("brand") String brand);

    @Query("SELECT DISTINCT c.origin FROM Cigar c ORDER BY c.origin")
    List<Origin> findAllOrigins();

    @Query("SELECT DISTINCT c.strength FROM Cigar c ORDER BY c.strength")
    List<Strength> findAllStrengths();

    @Query("SELECT DISTINCT c.brand FROM Cigar c ORDER BY c.brand")
    List<String> findAllBrands();
}
