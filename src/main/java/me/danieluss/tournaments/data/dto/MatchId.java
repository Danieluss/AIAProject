package me.danieluss.tournaments.data.dto;

import lombok.Data;
import me.danieluss.tournaments.data.model.Match;

@Data
public class MatchId {

    private Match match;
    private Integer id;

}
