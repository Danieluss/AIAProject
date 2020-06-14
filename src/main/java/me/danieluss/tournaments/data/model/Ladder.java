package me.danieluss.tournaments.data.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Ladder {

    @Id
    private Long id;

    @OneToOne
    private Tournament tournament;

    @OneToMany(mappedBy = "ladder")
    private List<Match> matches;

    public Ladder(Tournament tournament) {
        this.tournament = tournament;
        this.matches = new ArrayList<>();
    }

}
