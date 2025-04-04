package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewMatchType {

    @JsonProperty("match_type_name")
    @Size(max = 200, message = "Match type name no more than 200 characters")
    private String matchTypeName;

    @JsonProperty("player_number")
    @Min(value = 1, message = "playerNumber must be at least 1")
    @Max(value = 19, message = "playerNumber must not exceed 19")
    private int playerNumber;

    @JsonProperty("match_duration")
    @DecimalMin(value = "0.5", message = "matchDuration must be at least 0.5")
    @DecimalMax(value = "0.75", message = "matchDuration must not exceed 0.75")
    private double matchDuration;

    @JsonProperty("finish_type")
    private FinishType finishType;

}
