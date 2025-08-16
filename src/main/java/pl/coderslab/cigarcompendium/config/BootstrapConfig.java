package pl.coderslab.cigarcompendium.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.coderslab.cigarcompendium.model.AppConfig;
import pl.coderslab.cigarcompendium.repository.AppConfigRepository;

@Configuration
public class BootstrapConfig {
    @Bean
    CommandLineRunner ensureAppConfig(AppConfigRepository repo) {
        return args -> repo.findById(1L).orElseGet(() -> {
            AppConfig cfg = new AppConfig();
            cfg.setId(1L);
            cfg.setSetupOpen(true);
            return repo.save(cfg);
        });
    }
}
