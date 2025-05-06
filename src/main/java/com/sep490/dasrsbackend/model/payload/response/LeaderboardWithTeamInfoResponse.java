package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardWithTeamInfoResponse {

    @JsonProperty("round_id")
    private Long roundId;

    @JsonProperty("round_name")
    private String roundName;

    @JsonProperty("finish_type")
    private FinishType finishType;

    @JsonProperty("fastest_lap_time")
    private FastestLapTimeTeam fastestLapTime;

    @JsonProperty("top_speed")
    private TopSpeedTeam topSpeed;

    @JsonProperty("content")
    private List<LeaderboardDataWithTeamInfo> content;

    @JsonProperty("page_no")
    private int pageNo;

    @JsonProperty("page_size")
    private int pageSize;

    @JsonProperty("total_elements")
    private long totalElements;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("last")
    private boolean last;
}
