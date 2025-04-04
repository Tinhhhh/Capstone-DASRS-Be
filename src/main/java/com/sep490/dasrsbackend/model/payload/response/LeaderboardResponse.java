package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponse {

    @JsonProperty(value = "round_id", index = 1)
    private Long roundId;

    @JsonProperty(value = "finish_type", index = 2)
    private FinishType finishType;

    @JsonProperty(value = "fastest_lap_time", index = 3)
    private FastestLapTimeTeam fastestLapTime;

    @JsonProperty(value = "top_speed", index = 4)
    private TopSpeedTeam topSpeed;

    @JsonProperty("leaderboard_id")
    private Long leaderboardId;

    @JsonProperty("ranking")
    private int ranking;

    @JsonProperty("team_score")
    private double teamScore;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("team_id")
    private Long teamId;


}
