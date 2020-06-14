package me.danieluss.tournaments.data.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Entity
@NoArgsConstructor
@Table(uniqueConstraints =
        {@UniqueConstraint(columnNames = {"license", "tournament_id", "user_id"}),
                @UniqueConstraint(columnNames = {"rank", "tournament_id", "user_id"})})
public class TournamentAppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @EqualsAndHashCode.Exclude
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @NotNull
    private Tournament tournament;

    @EqualsAndHashCode.Exclude
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @NotNull
    private AppUser user;

    @NotBlank
    private String license;
    private double rank;

    public boolean same(TournamentAppUser winner2) {
        return getUser().getEmail().equalsIgnoreCase(winner2.getUser().getEmail());
    }

    public boolean sameEmail(AppUser appUser) {
        return appUser.getEmail().equalsIgnoreCase(user.getEmail());
    }
}
