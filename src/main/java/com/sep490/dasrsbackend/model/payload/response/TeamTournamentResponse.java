package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeamTournamentResponse {

    @JsonProperty("team_id")
    private Long id;

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("team_tag")
    private String teamTag;

    @JsonProperty("account_id")
    private UUID accountId;

    @JsonProperty("account_name")
    private String playerName;
}
