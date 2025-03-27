package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponse {

    @JsonProperty("leaderboard_id")
    private Long id;

    @JsonProperty("ranking")
    private int ranking;

    @JsonProperty("team_score")
    private double teamScore;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("team_id")
    private Long teamId;

    @JsonProperty("round_id")
    private Long roundId;
}
