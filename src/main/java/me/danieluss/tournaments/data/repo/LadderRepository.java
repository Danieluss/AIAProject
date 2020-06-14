package me.danieluss.tournaments.data.repo;

import me.danieluss.tournaments.data.model.Ladder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LadderRepository extends JpaRepository<Ladder, Long> {



}
