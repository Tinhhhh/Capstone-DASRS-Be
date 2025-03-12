package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentResponse {

    @JsonProperty("tournament_id")
    private Long id;

    @JsonProperty("tournament_name")
    private String tournamentName;

    private String context;

    @JsonProperty("team_number")
    private int teamNumber;

    private TournamentStatus status;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("round_list")
    private List<RoundResponse> roundList;

    @JsonProperty("team_list")
    private List<TeamTournamentResponse> teamList;

}
