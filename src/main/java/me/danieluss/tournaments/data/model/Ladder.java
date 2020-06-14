package me.danieluss.tournaments.data.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Ladder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private Tournament tournament;

    @OneToMany(mappedBy = "ladder", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<Match> matches;

    public Ladder(Tournament tournament) {
        this.tournament = tournament;
        this.matches = new ArrayList<>();
    }

}
