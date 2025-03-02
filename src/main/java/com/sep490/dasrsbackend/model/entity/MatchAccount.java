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
@Table(name = "match_account")
public class MatchAccount {

    @EmbeddedId
    private MatchAccountId id;

    @Column(name = "team_tag")
    private String teamTag;

    @Column(name = "car_friction")
    private double carFriction;

    @Column(name = "car_acceleration")
    private double carAcceleration;

    @ManyToOne
    @MapsId("matchId")
    @JoinColumn(name = "match_Id")
    Match match;

    @ManyToOne
    @MapsId("accountId")
    @JoinColumn(name = "account_Id")
    Account account;

}
