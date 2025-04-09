package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarLoadoutResponse {

    @JsonProperty("car_id")
    private Long carId;

    @JsonProperty("account_id")
    private UUID accountId;

    @JsonProperty("car_name")
    private String carName;

    @JsonProperty("maximum_torque")
    private double maxTorqueAsNM;

    @JsonProperty("traction_helper_strength")
    private double tractionHelperStrength;

    @JsonProperty("max_brake_torque")
    private double maxBrakeTorque;

    @JsonProperty("front_camper")
    private double frontCamper;

    @JsonProperty("rear_camper")
    private double rearCamper;

    @JsonProperty("front_ssr")
    private double frontSSR;

    @JsonProperty("rear_ssr")
    private double rearSSR;

    @JsonProperty("front_suspension")
    private double frontSuspension;

    @JsonProperty("rear_suspension")
    private double rearSuspension;

    @JsonProperty("front_ssd")
    private double frontSSD;

    @JsonProperty("rear_ssd")
    private double rearSSD;

}
