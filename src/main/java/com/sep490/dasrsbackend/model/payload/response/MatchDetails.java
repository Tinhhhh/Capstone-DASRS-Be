package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchDetails {

    @JsonProperty("match_id")
    private Long matchId;

    @JsonProperty("match_name")
    private String matchName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("finish_type")
    private String finishType;

    @JsonProperty("fastest_lap_time")
    private FastestLapTimeTeam fastestLapTime;

    @JsonProperty("top_speed")
    private TopSpeedTeam topSpeed;
}

