package me.danieluss.tournaments.service;

import me.danieluss.tournaments.data.dto.TournamentRegistration;
import me.danieluss.tournaments.data.model.*;
import me.danieluss.tournaments.data.repo.ImageRepository;
import me.danieluss.tournaments.data.repo.TournamentAppUserRepository;
import me.danieluss.tournaments.data.repo.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.*;

@Service
public class TournamentService {

    private final ImageRepository imageRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentAppUserRepository tournamentAppUserRepository;

    @Autowired
    public TournamentService(ImageRepository imageRepository, TournamentRepository tournamentRepository, TournamentAppUserRepository tournamentAppUserRepository) {
        this.imageRepository = imageRepository;
        this.tournamentRepository = tournamentRepository;
        this.tournamentAppUserRepository = tournamentAppUserRepository;
    }

    @Transactional
    public void save(Tournament tournament, Tournament existingTournament) {
        if (existingTournament != null) {
            imageRepository.deleteByTournament(existingTournament);
        }
        tournamentRepository.save(tournament);
    }

    @Transactional
    public boolean signUp(TournamentRegistration registration, AppUser user) {
        boolean signedUp = false;
        Tournament tournament = tournamentRepository.getOne(registration.getTournamentId());
        if (tournament.getParticipants().size() < tournament.getMaxParticipants() && canApply(tournament, user)) {
            TournamentAppUser tournamentAppUser = new TournamentAppUser();
            tournamentAppUser.setTournament(tournament);
            tournamentAppUser.setUser(user);
            tournamentAppUser.setLicense(registration.getLicense());
            tournamentAppUser.setRank(registration.getRank());
            tournamentAppUserRepository.save(tournamentAppUser);
            signedUp = true;
        }
        return signedUp;
    }

    public Ladder fill(Ladder ladder, Set<TournamentAppUser> participantsSet, String eliminationMode) {
        if (eliminationMode.equals("SINGLE")) {
            List<TournamentAppUser> participants = new ArrayList<>(participantsSet);
            participants.sort((p1, p2) -> (int) ceil(p1.getRank() - p2.getRank()));
            int noParticipants = participants.size();
            int noMatches = (int) pow(2, floor(log(noParticipants - 1)/log(2)));;
            List<Match> previousRound = new ArrayList<>();
            for (int i = 0; i < noMatches; i++) {
                previousRound.add(new Match());
            }
            for (int i = 0; i < noParticipants - noMatches; i++) {
                previousRound.get(i).setRound(0);
                previousRound.get(i).setUser1(participants.get(2*i));
                previousRound.get(i).setUser2(participants.get(2*i + 1));
            }
            for (int i = noParticipants - noMatches; i < noMatches; i++) {
                previousRound.get(i).setRound(0);
                previousRound.get(i).setUser1(participants.get(i + noParticipants - noMatches));
            }
            List<Match> matches = new ArrayList<>(previousRound);
            noMatches >>= 1;
            int roundNo = 1;
            while (noMatches >= 1) {
                List<Match> thisRound = new ArrayList<>();
                for (int i = 0; i < noMatches; i++) {
                    thisRound.add(new Match());
                    thisRound.get(i).setRound(roundNo);
                }
                previousRound = thisRound;
                matches.addAll(previousRound);
                noMatches >>= 1;
                roundNo++;
            }

            ladder.setMatches(matches);
        }
        return ladder;
    }

    public void updateWinners(Match match, Match parallelMatch, Match nextMatch, String eliminationMode) {
        if (eliminationMode.equals("SINGLE")) {
            TournamentAppUser winner1 = match.getWinner();
            if (winner1 != null) {
                match.setClosed(true);
            }
            TournamentAppUser winner2 = parallelMatch.getWinner();
            if (winner2 != null) {
                parallelMatch.setClosed(true);
            }
            if (winner1 != null && winner2 != null) {
                nextMatch.setUser1(winner1);
                nextMatch.setUser2(winner2);
            }
        }
    }

    public boolean canApply(Tournament tournament, AppUser appUser) {
        return !appUser.getParticipatedTournaments().stream().map(tournamentAppUser1 -> tournamentAppUser1.getTournament().getId()).collect(Collectors.toSet()).contains(tournament.getId())
                && tournament.getParticipants().size() < tournament.getMaxParticipants()
                && (new Date()).before(tournament.getApplicationDeadline());
    }

}
