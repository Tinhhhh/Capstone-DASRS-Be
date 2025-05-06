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
public class LeaderboardForAll {

    @JsonProperty(value = "round_id", index = 1)
    private Long roundId;

    @JsonProperty(value = "round_name", index = 2)
    private String roundName;

    @JsonProperty(value = "finish_type", index = 3)
    private FinishType finishType;

    @JsonProperty(value = "fastest_lap_time", index = 4)
    private FastestLapTimeTeam fastestLapTime;

    @JsonProperty(value = "top_speed", index = 5)
    private TopSpeedTeam topSpeed;

    @JsonProperty(value = "leaderboard_list", index = 6)
    private List<LeaderboardChildForAll> content;

    @JsonProperty(value = "page_no", index = 7)
    private int pageNo;
    @JsonProperty(value = "page_size", index = 8)
    private int pageSize;
    @JsonProperty(value = "total_elements", index = 9)
    private long totalElements;
    @JsonProperty(value = "total_pages", index = 10)
    private int totalPages;
    @JsonProperty(value = "last", index = 11)
    private boolean last;
}
