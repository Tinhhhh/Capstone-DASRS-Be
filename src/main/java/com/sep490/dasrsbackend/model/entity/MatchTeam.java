package com.sep490.dasrsbackend.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "match_team")
public class MatchTeam {

    @EmbeddedId
    private MatchTeamId id;

    @Column(name = "team_tag")
    private String teamTag;

    @Column(name = "car_name")
    private String carName;

    @Column(name = "wheels")
    private Integer wheels;

    @Column(name = "colors")
    private Integer colors;

    @Column(name = "front_camper")
    private Double frontCamper;

    @Column(name = "rear_camper")
    private Double rearCamper;

    @Column(name = "front_ssr")
    private Double frontSSR;

    @Column(name = "rear_ssr")
    private Double rearSSR;

    @Column(name = "front_suspension")
    private Double frontSuspension;

    @Column(name = "rear_suspension")
    private Double rearSuspension;

    @Column(name = "front_ssd")
    private Double frontSSD;

    @Column(name = "rear_ssd")
    private Double rearSSD;

    @Column(name = "engine")
    private Double engine;

    @Column(name = "handling")
    private Double handling;

    @Column(name = "brake")
    private Double brake;

    @Column(name = "decals")
    private Integer decals;

    @Column(name = "neon")
    private Integer neon;

    @Column(name = "spoilers")
    private Integer spoilers;

    @Column(name = "sirens")
    private Boolean sirens;

    @ManyToOne
    @MapsId("matchId")
    @JoinColumn(name = "match_Id")
    Match match;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

    @ManyToOne
    @JoinColumn(name = "score_attribute_id")
    private ScoreAttribute scoreAttribute;

    @ManyToOne
    @MapsId("teamId")
    @JoinColumn(name = "team_Id")
    Team team;

}
