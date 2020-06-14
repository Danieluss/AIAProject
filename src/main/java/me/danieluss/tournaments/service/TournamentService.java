package me.danieluss.tournaments.service;

import me.danieluss.tournaments.data.dto.TournamentRegistration;
import me.danieluss.tournaments.data.dto.WinInfo;
import me.danieluss.tournaments.data.model.*;
import me.danieluss.tournaments.data.repo.*;
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
    private final LadderRepository ladderRepository;
    private final MatchRepository matchRepository;
    private final TournamentAppUserRepository tournamentAppUserRepository;

    @Autowired
    public TournamentService(ImageRepository imageRepository, TournamentRepository tournamentRepository, LadderRepository ladderRepository, MatchRepository matchRepository, TournamentAppUserRepository tournamentAppUserRepository) {
        this.imageRepository = imageRepository;
        this.tournamentRepository = tournamentRepository;
        this.ladderRepository = ladderRepository;
        this.matchRepository = matchRepository;
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
                previousRound.get(i).setWinner1(participants.get(i + noParticipants - noMatches));
                previousRound.get(i).setWinner2(participants.get(i + noParticipants - noMatches));
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

            for(var match: matches) {
                match.setLadder(ladder);
            }
            ladder.setMatches(matches);
        }
        return ladder;
    }

    public void updateWinners(Match match, Match parallelMatch, Match nextMatch, String eliminationMode) {
        if (eliminationMode.equals("SINGLE")) {
            if(match!= null) {
                TournamentAppUser winner1 = match.getWinner();
                if (winner1 != null) {
                    match.setClosed(true);
                }
                if (parallelMatch != null) {
                    TournamentAppUser winner2 = parallelMatch.getWinner();
                    if(winner2 != null) {
                        parallelMatch.setClosed(true);
                    }
                    if (nextMatch != null && winner1 != null && winner2 != null) {
                        nextMatch.setUser1(winner1);
                        nextMatch.setUser2(winner2);
                    }
                }
            }
        }
    }

    public boolean canApply(Tournament tournament, AppUser appUser) {
        return !appUser.getParticipatedTournaments().stream().map(tournamentAppUser1 -> tournamentAppUser1.getTournament().getId()).collect(Collectors.toSet()).contains(tournament.getId())
                && tournament.getParticipants().size() < tournament.getMaxParticipants()
                && (new Date()).before(tournament.getApplicationDeadline());
    }

    private Ladder makeLadder(Tournament tournament) {
        Ladder ladder = new Ladder(tournament);
        return fill(ladder, tournament.getParticipants(), tournament.getEliminationMode());
    }

    @Transactional
    public void tryToMakeLadder(Tournament oldTournament) {
        Tournament tournament = tournamentRepository.getOne(oldTournament.getId());
        if ((new Date()).after(tournament.getApplicationDeadline())) {
            if (tournament.getParticipants().size() < 2) {
                // handle tournament cancellation
                return;
            }
            Ladder ladder = makeLadder(tournament);
            tournament.setLadder(ladder);
            ladder.setTournament(tournament);
            tournamentRepository.save(tournament);
        }
    }

    @Transactional
    public void tryToUpdateWinners(WinInfo winInfo, AppUser user) {
        Tournament tournament = tournamentRepository.getOne(winInfo.getTournamentId());
        Ladder ladder = tournament.getLadder();
        List<Match> matches = ladder.getMatches();
        Match match = matches.get(winInfo.getNoMatch());
        if (match.getUser1().sameEmail(user)) {
            if (winInfo.getWon()) {
                match.setWinner1(match.getUser1());
            } else {
                match.setWinner1(match.getUser2());
            }
        } else {
            if (winInfo.getWon()) {
                match.setWinner2(match.getUser2());
            } else {
                match.setWinner2(match.getUser1());
            }
        }


        int thisMatch = winInfo.getNoMatch();
        int parallelMatchI = ((thisMatch >> 1) << 1) + (1 - (thisMatch % 2));

//       TODO proper sum of geo series or change to list of lists
        int ai = (matches.size() + 1) >> 1;
        int acc = ai;
        while (acc <= thisMatch) {
            ai >>= 1;
            acc += ai;
        }
        int nextMatchI = acc + ((thisMatch - acc + ai) / 2);
        Match nextMatch = nextMatchI < matches.size() ? matches.get(nextMatchI) : null;
        Match parallelMatch = nextMatchI < matches.size() ? matches.get(parallelMatchI) : null;
        updateWinners(match, parallelMatch, nextMatch, tournament.getEliminationMode());
        ladderRepository.save(ladder);
    }
}
