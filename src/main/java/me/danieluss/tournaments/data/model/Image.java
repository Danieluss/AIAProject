package me.danieluss.tournaments.data.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Tournament tournament;

    private String name;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] blob;
}
