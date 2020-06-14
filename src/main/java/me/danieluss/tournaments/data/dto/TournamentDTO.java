package me.danieluss.tournaments.data.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.danieluss.tournaments.data.model.Tournament;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class TournamentDTO {

    @NotBlank
    private String name;
    @NotBlank
    private String discipline;
    @NotBlank
    private String place;
    @NotBlank
    private String eliminationMode;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date time;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date applicationDeadline;

    @Min(2) @NotNull
    private Integer maxParticipants;
    private List<MultipartFile> images;

    public TournamentDTO(Tournament tournament) {
        this.name = tournament.getName();
        this.discipline = tournament.getDiscipline();
        this.place = tournament.getPlace();
        this.time = tournament.getTime();
        this.applicationDeadline = tournament.getApplicationDeadline();
        this.maxParticipants = tournament.getMaxParticipants();
        this.images = new ArrayList<>();
        this.eliminationMode = tournament.getEliminationMode();
    }

}
