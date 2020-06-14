package me.danieluss.tournaments.data.repo;

import me.danieluss.tournaments.data.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByEmailIgnoreCase(String email);
}
