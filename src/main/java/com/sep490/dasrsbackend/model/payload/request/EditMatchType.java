package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.MatchTypeStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditMatchType {

    @JsonProperty("match_type_name")
    @Size(max = 200, message = "Match type name no more than 200 characters")
    private String matchTypeName;

    @JsonProperty("player_number")
    @Min(value = 1, message = "playerNumber must be at least 1")
    @Max(value = 5, message = "playerNumber must not exceed 5")
    private int playerNumber;

    @Min(value = 1, message = "teamNumber must be at least 1")
    @Max(value = 19, message = "teamNumber must not exceed 19")
    @JsonProperty("team_number")
    private int teamNumber;

    @JsonProperty("match_duration")
    @DecimalMin(value = "0.5", message = "matchDuration must be at least 0.5")
    @DecimalMax(value = "0.1", message = "matchDuration must not exceed 0.1")
    private double matchDuration;

    @JsonProperty("status")
    private MatchTypeStatus status;

}
