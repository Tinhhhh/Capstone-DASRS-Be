package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.MatchForm;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RematchResponse {

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

    @JsonProperty("complaint_title")
    private String title;

    @JsonProperty("complaint_description")
    private String description;

    private List<TeamTournamentResponse> teams;
}
