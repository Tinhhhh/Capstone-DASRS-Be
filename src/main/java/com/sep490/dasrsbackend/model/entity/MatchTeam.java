package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

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

    @Column(name = "car_friction")
    private double carFriction;

    @Column(name = "car_acceleration")
    private double carAcceleration;

    @Column(name = "car_max_RPM")
    private double carMaxRPM;

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
