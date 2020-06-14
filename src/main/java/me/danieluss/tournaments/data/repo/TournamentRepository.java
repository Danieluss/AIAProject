package me.danieluss.tournaments.data.repo;

import me.danieluss.tournaments.data.model.Tournament;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long>, JpaSpecificationExecutor<Tournament> {

    Optional<Tournament> findById(Long id);

    @Query(value = "SELECT t FROM tournament_app_user tap join tournament t on tap.tournament_id = t.id WHERE user_id = ?1",
            countQuery = "SELECT count(*) FROM tournament_app_user tap join tournament t on tap.tournament_id = t.id WHERE user_id = ?1",
            nativeQuery = true)
    Page<Tournament> findTournamentsByUser(Long id, Pageable pageable);
}
