package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.MatchTypeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "match_type")
public class MatchType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_type_id")
    @JsonProperty("match_type_id")
    private Long id;

    @Column(name = "match_type_name", nullable = false)
    private String matchTypeName;

    @Column(name = "match_type_code", nullable = false)
    private String matchTypeCode;

    @Column(name = "match_duration", nullable = false)
    private double matchDuration;

    @Column(name = "player_number", nullable = false)
    private int playerNumber;

    @Column(name = "team_number", nullable = false)
    private int teamNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MatchTypeStatus status;

    @OneToMany(mappedBy = "matchType")
    private List<Round> roundList;

}
