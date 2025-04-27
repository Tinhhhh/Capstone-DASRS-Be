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
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardTournament {

    @JsonProperty(value = "round_id", index = 1)
    private Long roundId;

    @JsonProperty(value = "round_name", index = 2)
    private String roundName;

    @JsonProperty(value = "description", index = 3)
    private String description;

    @JsonProperty(value = "finish_type", index = 4)
    private FinishType finishType;

    @JsonProperty(value = "fastest_lap_time", index = 5)
    private FastestLapTimeTeam fastestLapTime;

    @JsonProperty(value = "top_speed", index = 6)
    private TopSpeedTeam topSpeed;

    @JsonProperty(value = "content", index = 7)
    private List<LeaderboardTournamentChild> content;

}
