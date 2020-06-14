package me.danieluss.tournaments.service;

import me.danieluss.tournaments.data.model.AppUser;
import me.danieluss.tournaments.data.model.Ladder;
import me.danieluss.tournaments.data.model.Match;
import me.danieluss.tournaments.data.model.TournamentAppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TournamentServiceTest {

    @Autowired
    TournamentService tournamentService;

    private TournamentAppUser prepareUser(int i) {
        AppUser appUser = new AppUser();
        appUser.setEmail(UUID.randomUUID().toString().substring(0, 16) + "@mock.com");
        TournamentAppUser tournamentAppUser = new TournamentAppUser();
        tournamentAppUser.setId(i);
        tournamentAppUser.setRank(i);
        tournamentAppUser.setLicense(String.valueOf(i));
        tournamentAppUser.setUser(appUser);
        return tournamentAppUser;
    }

    private void check(Ladder ladder, int fr, int to, int mode) {
        List<Match> matches = ladder.getMatches();
        boolean acc = true;
        if (mode == 0) {
            for (int i = fr; i < to; i++) {
                acc = acc && matches.get(i).getUser1() != null;
                acc = acc && matches.get(i).getUser2() != null;
            }
        } else if (mode == 1) {
            for (int i = fr; i < to; i++) {
                acc = acc && matches.get(i).getUser1() != null;
                acc = acc && matches.get(i).getUser2() == null;
            }
        } else if (mode == 2) {
            for (int i = fr; i < to; i++) {
                acc = acc && matches.get(i).getUser1() == null;
                acc = acc && matches.get(i).getUser2() == null;
            }
        }
        assertTrue(acc);
    }

    @Test
    public void testSingleFill() {
        Ladder ladder = new Ladder();
        List<Integer> cases = List.of(2, 3, 7, 8, 9, 15, 16, 17);
        for(int c : cases) {
            Set<TournamentAppUser> userSet = IntStream.range(0, c).boxed().map(this::prepareUser).collect(Collectors.toSet());

            tournamentService.fill(ladder, userSet, "SINGLE");

            int noParticipants = userSet.size();
            int noMatches = (int) pow(2, floor(log(noParticipants - 1)/log(2)));
            check(ladder, 0, noParticipants - noMatches, 0);
            check(ladder, noParticipants - noMatches, noMatches, 1);
            check(ladder, noMatches, 2*(noMatches - 1) + 1, 2);
            assertEquals((int) pow(2, (ceil(log(c)/log(2)))) - 1, ladder.getMatches().size());
        }
    }

    private Match prepareMatch(TournamentAppUser w0, TournamentAppUser w1) {
        Match match = new Match();
        match.setUser1(w0);
        match.setUser2(w1);
        match.setWinner1(w1);
        match.setWinner2(w1);
        return match;
    }

    @Test
    public void testSingleUpdate() {
        TournamentAppUser w10 = prepareUser(1);
        TournamentAppUser w11 = prepareUser(2);
        TournamentAppUser w20 = prepareUser(3);
        TournamentAppUser w21 = prepareUser(4);
        Match match = prepareMatch(w10, w11);
        Match parallelMatch = prepareMatch(w20, w21);
        Match nextMatch = new Match();

        tournamentService.updateWinners(match, parallelMatch, nextMatch, "SINGLE");

        assertTrue(match.isClosed());
        assertTrue(parallelMatch.isClosed());
        assertFalse(nextMatch.isClosed());
        assertEquals(nextMatch.getUser1(), w11);
        assertEquals(nextMatch.getUser2(), w21);
    }

}
