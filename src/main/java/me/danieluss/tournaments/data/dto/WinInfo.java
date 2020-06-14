package me.danieluss.tournaments.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WinInfo {

    private Long tournamentId;
    private Long matchId;
    private Integer noMatch;
    private Boolean won;

}
