package me.danieluss.tournaments.data.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Match {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn
    private TournamentAppUser user1;
    @ManyToOne
    @JoinColumn
    private TournamentAppUser user2;

    @ManyToOne
    @JoinColumn
    private TournamentAppUser winner1;
    @ManyToOne
    @JoinColumn
    private TournamentAppUser winner2;

    private boolean closed;
    private int round;

    @ManyToOne
    @JoinColumn
    private Ladder ladder;

    @Transient
    public TournamentAppUser getWinner() {
        if (getUser2() == null) {
            return getUser1();
        }
        if (winner1 == null || winner2 == null) {
            return null;
        }
        if (getWinner1().same(getWinner2())) {
            return getWinner1();
        }
        return null;
    }

    public boolean hasUser(AppUser appUser) {
        return (user1 != null && user1.sameEmail(appUser)) ||
                (user2 != null && user2.sameEmail(appUser));
    }

}
