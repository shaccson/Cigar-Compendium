package pl.coderslab.cigarcompendium.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class AppConfig {

    @Id
    private Long id = 1L;
    private boolean setupOpen = true;
}
