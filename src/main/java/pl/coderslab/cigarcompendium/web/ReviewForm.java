package pl.coderslab.cigarcompendium.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewForm {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 200)
    private String comment;

    @NotNull
    private Long cigarId;
}
