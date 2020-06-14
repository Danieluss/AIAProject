package me.danieluss.tournaments.data.repo;

import me.danieluss.tournaments.data.model.Image;
import me.danieluss.tournaments.data.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    void deleteByTournament(Tournament tournament);
}
