package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerResponseForLeaderboard {

    @JsonProperty("player_id")
    private UUID playerId;

    @JsonProperty("player_name")
    private String playerName;

    @JsonProperty("score")
    private double score;
}
