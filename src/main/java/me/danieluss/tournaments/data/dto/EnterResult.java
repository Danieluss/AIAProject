package me.danieluss.tournaments.data.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class EnterResult {

    private Long tournamendId;
    private Integer matchId;
    @NotBlank
    private String won;

}
