package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.MatchForm;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {

    @JsonProperty("match_id")
    private Long id;

    @JsonProperty("match_name")
    private String matchName;

    @JsonProperty("match_code")
    private String matchCode;

    @JsonProperty("time_start")
    private String timeStart;

    @JsonProperty("time_end")
    private String timeEnd;

    @JsonProperty("status")
    private MatchStatus status;

    @JsonProperty("match_form")
    private MatchForm matchForm;

    private List<TeamTournamentResponse> teams;
}
