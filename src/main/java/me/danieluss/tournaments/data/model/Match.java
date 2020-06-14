package me.danieluss.tournaments.data.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.userdetails.User;

import javax.persistence.*;
import java.security.Principal;

@Data
@Entity
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn
    private TournamentAppUser user1;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn
    private TournamentAppUser user2;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn
    private TournamentAppUser winner1;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
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

    public String get1stUserString() {
        if (getUser1() == null)
            return "-";
        AppUser user = getUser1().getUser();

        return getUserString(user);
    }

    public String get2ndUserString() {
        if (getUser2() == null)
            return "-";
        AppUser user = getUser2().getUser();
        return getUserString(user);
    }

    public String getUserString(AppUser user) {
        if (user == null) {
            return "-";
        }
        return user.getMatchString();
    }

    public String getWinnerString() {
        return getWinner() != null ? getWinner().getUser().getMatchString() : "vs";
    }

    public boolean hasEmail(Object principal) {
        if (principal instanceof User) {
            AppUser appUser = new AppUser();
            appUser.setEmail(((User) principal).getUsername());
            return hasUser(appUser);
        }
        return false;
    }
}
