package pl.coderslab.cigarcompendium.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "review",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","cigar_id"})
)
@Getter @Setter
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1) @Max(5)
    @Column(nullable = false)
    private int rating;

    @Size(max = 1000)
    private String comment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Cigar cigar;
}
