package me.danieluss.tournaments.service;

import me.danieluss.tournaments.data.model.Ladder;
import me.danieluss.tournaments.data.model.Tournament;
import me.danieluss.tournaments.data.repo.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

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
            Tournament tournament = tournamentRepository.getOne(oldTournament.getId());
            if ((new Date()).after(tournament.getApplicationDeadline())) {
                if (tournament.getParticipants().size() < 2) {
                    // handle tournament cancellation
                    return;
                }
                Ladder ladder = makeLadder(tournament);
                tournament.setLadder(ladder);
                tournamentRepository.save(tournament);
            }
        }, oldTournament.getApplicationDeadline());
    }

    private Ladder makeLadder(Tournament tournament) {
        Ladder ladder = new Ladder(tournament);
        return tournamentService.fill(ladder, tournament.getParticipants(), tournament.getEliminationMode());
    }
}
