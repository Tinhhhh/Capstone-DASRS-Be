package com.sep490.dasrsbackend.converter;

import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.payload.response.TeamResponse;
import org.springframework.stereotype.Component;

@Component
public class TeamConverter {

    public TeamResponse convertToTeamResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getTeamName()) // Mapping teamName to name
                .tag(team.getTeamTag()) // Mapping teamTag to tag
                .disqualified(team.isDisqualified()) // Mapping isDisqualified to disqualified
                .status(team.getStatus())
                .build();
    }
}
