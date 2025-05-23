package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchCarData {

    @JsonProperty("match_code")
    private String matchCode;

    @JsonProperty("player_id")
    private UUID playerId;

    @JsonProperty("car_name")
    private String carName;

    @DecimalMin(value = "-10.0", message = "Team number must be at least -10.0")
    @DecimalMax(value = "10.0", message = "Team number must be at most 10.0")
    @JsonProperty("front_camper")
    private double frontCamper;

    @DecimalMin(value = "-10.0", message = "Team number must be at least -10.0")
    @DecimalMax(value = "10.0", message = "Team number must be at most 10.0")
    @JsonProperty("rear_camper")
    private double rearCamper;

    @DecimalMin(value = "10000.0", message = "Team number must be at least 10000.0")
    @DecimalMax(value = "100000.0", message = "Team number must be at most 100000.0")
    @JsonProperty("front_ssr")
    private double frontSSR;

    @DecimalMin(value = "10000.0", message = "Team number must be at least 10000.0")
    @DecimalMax(value = "100000.0", message = "Team number must be at most 100000.0")
    @JsonProperty("rear_ssr")
    private double rearSSR;

    @DecimalMax(value = "0.4", message = "Team number must be at most 0.4")
    @DecimalMin(value = "0.1", message = "Team number must be at least 0.1")
    @JsonProperty("front_suspension")
    private double frontSuspension;

    @DecimalMax(value = "0.4", message = "Team number must be at most 0.4")
    @DecimalMin(value = "0.1", message = "Team number must be at least 0.1")
    @JsonProperty("rear_suspension")
    private double rearSuspension;

    @DecimalMin(value = "1000.0", message = "Team number must be at least 1000.0")
    @DecimalMax(value = "10000.0", message = "Team number must be at most 10000.0")
    @JsonProperty("front_ssd")
    private double frontSSD;

    @DecimalMin(value = "1000.0", message = "Team number must be at least 1000.0")
    @DecimalMax(value = "10000.0", message = "Team number must be at most 10000.0")
    @JsonProperty("rear_ssd")
    private double rearSSD;

}
