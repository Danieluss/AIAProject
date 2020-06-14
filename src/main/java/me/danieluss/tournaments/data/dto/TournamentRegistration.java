package me.danieluss.tournaments.data.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class TournamentRegistration {

    @NotNull
    private Long tournamentId;
    @NotBlank
    private String license;
    @Min(0)
    private double rank;

}
