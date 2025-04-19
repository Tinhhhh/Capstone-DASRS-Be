package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import com.sep490.dasrsbackend.model.enums.MatchTeamStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchTeamResponse {

    @JsonProperty("player_id")
    private UUID playerId;

    @JsonProperty("player_name")
    private String playerName;

    @JsonProperty("match_team_id")
    private Long matchTeamId;

    @JsonProperty("assign_status")
    private MatchTeamStatus status;

}
