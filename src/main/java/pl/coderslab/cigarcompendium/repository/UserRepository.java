package pl.coderslab.cigarcompendium.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.coderslab.cigarcompendium.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}
