package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewEnvironment {

    @JsonProperty("environment_name")
    private String name;

    private double friction;

    private double visibility;

    @JsonProperty("brake_efficiency")
    private double brakeEfficiency;

    @JsonProperty("slip_angle")
    private double slipAngle;

    @JsonProperty("reaction_delay")
    private double reactionDelay;

}
