package pl.coderslab.cigarcompendium.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.coderslab.cigarcompendium.model.AppConfig;

public interface AppConfigRepository extends JpaRepository<AppConfig, Long> { }
