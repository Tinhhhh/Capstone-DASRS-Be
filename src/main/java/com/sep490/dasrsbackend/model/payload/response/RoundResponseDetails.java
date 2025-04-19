package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoundResponseDetails {
    @JsonProperty("round_id")
    private Long id;

    @JsonProperty("round_name")
    private String roundName;

    @JsonProperty("round_duration")
    private int roundDuration;

    @JsonProperty("lap_number")
    private int lapNumber;

    @JsonProperty("finish_type")
    private FinishType finishType;

    @JsonProperty("team_limit")
    private int teamLimit;

    @JsonProperty("is_last")
    private boolean isLast;

    private String description;

    private RoundStatus status;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("created_date")
    private String createDate;

    @JsonProperty("tournament_id")
    private Long tournamentId;

    @JsonProperty("scored_method_id")
    private Long scoredMethodId;

    @JsonProperty("environment_id")
    private Long environmentId;

    @JsonProperty("match_type_id")
    private Long matchTypeId;

    @JsonProperty("match_type_name")
    private String matchTypeName;

    @JsonProperty("map_id")
    private Long mapId;

    @JsonProperty("match_list")
    private List<MatchResponse> matchList;
}
