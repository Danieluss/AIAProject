package me.danieluss.tournaments.data.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
public class ConfirmationToken {

    public enum Type {
        CONFIRM_EMAIL, RESET_PASSWORD
    }

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    private Type type;
    private String confirmationToken;
    private String code;
    private boolean used;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @OneToOne(targetEntity = AppUser.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private AppUser appUser;

    public ConfirmationToken(AppUser appUser) {
        this.appUser = appUser;
        createdDate = new Date();
        confirmationToken = UUID.randomUUID().toString();
        id = UUID.randomUUID();
        used = false;
    }

}
