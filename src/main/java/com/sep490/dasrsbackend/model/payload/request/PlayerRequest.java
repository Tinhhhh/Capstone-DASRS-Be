package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PlayerRequest {

    @Schema(description = "Team name", example = "Global Esport")
    @JsonProperty("team_name")
    private String teamName;

    @Schema(description = "Verify if leader or not", example = "Yes/No")
    @JsonProperty("is_leader")
    private Boolean isLeader;
}
