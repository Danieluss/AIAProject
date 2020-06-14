package me.danieluss.tournaments.service;

import me.danieluss.tournaments.data.model.Ladder;
import me.danieluss.tournaments.data.model.Tournament;
import me.danieluss.tournaments.data.repo.TournamentRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Date;

@Service
public class ScheduleService {

    private final TaskScheduler taskScheduler;
    private final TournamentRepository tournamentRepository;
    private final TournamentService tournamentService;

    @Autowired
    public ScheduleService(TaskScheduler taskScheduler, TournamentRepository tournamentRepository, TournamentService tournamentService) {
        this.taskScheduler = taskScheduler;
        this.tournamentRepository = tournamentRepository;
        this.tournamentService = tournamentService;
    }

    public void schedule(Tournament oldTournament) {
        taskScheduler.schedule(() -> {
            tournamentService.tryToMakeLadder(oldTournament);
        }, oldTournament.getApplicationDeadline());
    }


}
