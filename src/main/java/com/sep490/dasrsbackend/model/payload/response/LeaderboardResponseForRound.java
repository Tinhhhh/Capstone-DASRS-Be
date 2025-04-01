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
public class LeaderboardResponseForRound {

    @JsonProperty(value = "round_id", index = 1)
    private Long roundId;

    @JsonProperty(value = "finish_type", index = 2)
    private FinishType finishType;

    @JsonProperty(value = "fastest_lap_time", index = 3)
    private FastestLapTimeTeam fastestLapTime;

    @JsonProperty(value = "top_speed", index = 4)
    private TopSpeedTeam topSpeed;

    @JsonProperty(value = "content", index = 5)
    private List<LeaderboardData> content;

    @JsonProperty(value = "page_no", index = 6)
    private int pageNo;
    @JsonProperty(value = "page_size", index = 7)
    private int pageSize;
    @JsonProperty(value = "total_elements", index = 8)
    private long totalElements;
    @JsonProperty(value = "total_pages", index = 9)
    private int totalPages;
    @JsonProperty(value = "last", index = 10)
    private boolean last;
}
