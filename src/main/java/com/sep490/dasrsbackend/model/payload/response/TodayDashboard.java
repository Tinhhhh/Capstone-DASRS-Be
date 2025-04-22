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
public class TodayDashboard {

    @JsonProperty("tournament_count")
    private int tournamentCount;

    @JsonProperty("team_count")
    private int teamCount;

    @JsonProperty("player_count")
    private int playerCount;

    @JsonProperty("round_count")
    private int roundCount;

    @JsonProperty("match_count")
    private int matchCount;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;
}
