package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentByTeamResponse {

    @JsonProperty("tournament_id")
    private Long id;

    @JsonProperty("tournament_name")
    private String tournamentName;

    @JsonProperty("tournament_context")
    private String context;

    @JsonProperty("team_number")
    private int teamNumber;

    private TournamentStatus status;

    @JsonProperty("is_started")
    private boolean isStarted;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("created_date")
    private String createdDate;
}
