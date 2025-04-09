package com.sep490.dasrsbackend.model.entity;

import com.sep490.dasrsbackend.model.enums.MatchTeamStatus;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_team_id")
    private Long id;

    @Column(name = "team_tag")
    private String teamTag;

    @Column(name = "car_name")
    private String carName;

    @Column(name = "maximum_torque")
    private double maxTorqueAsNM;

    @Column(name = "traction_helper_strength")
    private double tractionHelperStrength;

    @Column(name = "max_brake_torque")
    private double maxBrakeTorque;

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

    @Column(name = "score")
    private double score;

    @Column(name = "status")
    private MatchTeamStatus status;

    @Column(name = "attempt")
    private int attempt;

    @ManyToOne
    @JoinColumn(name = "match_Id")
    Match match;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

    @ManyToOne
    @JoinColumn(name = "score_attribute_id")
    private ScoreAttribute scoreAttribute;

    @ManyToOne
    @JoinColumn(name = "team_Id")
    Team team;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

}
