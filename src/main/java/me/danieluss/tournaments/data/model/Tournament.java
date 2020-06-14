package me.danieluss.tournaments.data.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.danieluss.tournaments.data.dto.TournamentDTO;
import org.apache.commons.lang3.builder.HashCodeExclude;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.IOException;
import java.util.*;

@Data
@Entity
@NoArgsConstructor
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    private String discipline;

    private String place;

    private String eliminationMode;

    @EqualsAndHashCode.Exclude
    @ManyToOne(cascade = CascadeType.PERSIST)
    private AppUser organizer;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<TournamentAppUser> participants;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    @Temporal(TemporalType.TIMESTAMP)
    private Date applicationDeadline;

    private Integer maxParticipants;

    @HashCodeExclude
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Image> images;

    private Integer noImages;

    @OneToOne(mappedBy = "tournament", cascade = CascadeType.ALL)
    private Ladder ladder;

    public Tournament(TournamentDTO tournamentDTO, AppUser appUser) {
        this.name = tournamentDTO.getName();
        this.discipline = tournamentDTO.getDiscipline();
        this.place = tournamentDTO.getPlace();
        this.time = tournamentDTO.getTime();
        this.applicationDeadline = tournamentDTO.getApplicationDeadline();
        this.maxParticipants = tournamentDTO.getMaxParticipants();
        this.organizer = appUser;
        this.participants = new HashSet<>();
        List<Image> list = new ArrayList<>();
        for (MultipartFile multipartFile : tournamentDTO.getImages()) {
            Image image = new Image();
            image.setTournament(this);
            try {
                image.setBlob(multipartFile.getBytes());
                image.setName(multipartFile.getName());
                list.add(image);
            } catch (IOException ignored) {
            }
        }
        this.noImages = list.size();
        this.eliminationMode = tournamentDTO.getEliminationMode();
        this.images = list;
    }
}
