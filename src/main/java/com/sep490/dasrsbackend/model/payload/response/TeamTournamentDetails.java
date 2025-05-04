package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.dto.ParticipantDTO;
import com.sep490.dasrsbackend.model.enums.TeamStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeamTournamentDetails {

    @JsonProperty("team_members")
    List<ParticipantDTO> teamMembers;
    @JsonProperty("team_id")
    private Long id;
    @JsonProperty("team_name")
    private String teamName;
    @JsonProperty("team_tag")
    private String teamTag;
    private TeamStatus status;

}
