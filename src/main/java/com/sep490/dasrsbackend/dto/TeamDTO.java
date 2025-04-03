package com.sep490.dasrsbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TeamDTO {

    @JsonProperty("team_id")
    private Long teamId;

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("team)tag")
    private String teamTag;
}