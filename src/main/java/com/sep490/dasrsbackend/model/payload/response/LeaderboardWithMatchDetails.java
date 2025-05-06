package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardWithMatchDetails {

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

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("team_tag")
    private String teamTag;

    @JsonProperty("matches")
    private List<MatchDetails> matches;
}

