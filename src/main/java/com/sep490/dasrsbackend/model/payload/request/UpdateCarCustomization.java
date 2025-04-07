package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCarCustomization {

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

//    @DecimalMin(value = "200.0", message = "Team number must be at least 200.0")
//    @DecimalMax(value = "1000.0", message = "Team number must be at most 1000.0")
//    private double engine;
//
//    @DecimalMin(value = "200.0", message = "Team number must be at least 200.0")
//    @DecimalMax(value = "1000.0", message = "Team number must be at most 1000.0")
//    private double handling;
//
//    @DecimalMin(value = "200.0", message = "Team number must be at least 200.0")
//    @DecimalMax(value = "1000.0", message = "Team number must be at most 1000.0")
//    private double brake;

}
