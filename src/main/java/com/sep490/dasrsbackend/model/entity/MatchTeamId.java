package com.sep490.dasrsbackend.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MatchTeamId implements Serializable {
    //    @Column(name = "match_id")
    private Long matchId;

    //    @Column(name="account_id")
    private Long teamId;
}
