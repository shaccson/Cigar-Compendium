package pl.coderslab.cigarcompendium.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Cigar {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank private String name;
    @NotBlank private String brand;

    @Enumerated(EnumType.STRING) @NotNull
    private Origin origin;

    @Enumerated(EnumType.STRING) @NotNull
    private Strength strength;

    private String flavor;

    @Size(max = 255)
    private String imagePath;
}
