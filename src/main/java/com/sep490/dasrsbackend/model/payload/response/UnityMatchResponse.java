package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import com.sep490.dasrsbackend.model.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnityMatchResponse {

    @JsonProperty(index = 1)
    private List<TeamTournamentResponse> teams;

    @JsonProperty("match_id")
    private Long matchId;

    @JsonProperty("scored_method_id")
    private Long scoredMethodId;

    @JsonProperty("match_name")
    private String matchName;

    @JsonProperty("match_code")
    private String matchCode;

    @JsonProperty("time_start")
    private String timeStart;

    @JsonProperty("time_end")
    private String timeEnd;

    @JsonProperty(value = "round_duration")
    private int roundDuration;

    @JsonProperty(value = "lap_number")
    private int lapNumber;

    @JsonProperty(value = "finish_type")
    private FinishType finishType;

    @JsonProperty("status")
    private MatchStatus status;

    @JsonProperty("resource_id")
    private Long resourceId;

    @JsonProperty("resource_name")
    private String resourceName;

    @JsonProperty("resource_type")
    private ResourceType resourceType;

    @JsonProperty("environment_id")
    private Long environmentId;

    @JsonProperty("environment_name")
    private String environmentName;

}
