package me.danieluss.tournaments.data.repo;

import me.danieluss.tournaments.data.model.Tournament;
import me.danieluss.tournaments.data.model.TournamentAppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentAppUserRepository extends JpaRepository<TournamentAppUser, Long>, JpaSpecificationExecutor<TournamentAppUser> {

}
