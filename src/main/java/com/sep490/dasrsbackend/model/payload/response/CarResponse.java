package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarResponse {

    @JsonProperty("car_id")
    private Long id;

    @JsonProperty("car_name")
    private String carName;

    @JsonProperty("maximum_torque")
    private double maxTorqueAsNM;

    @JsonProperty("minimum_engine_rpm")
    private double minEngineRPM;

    @JsonProperty("maximum_engine_rpm")
    private double maxEngineRPM;

    @JsonProperty("shift_up_rpm")
    private double shiftUpRPM;

    @JsonProperty("shift_down_rpm")
    private double shiftDownRPM;

    @JsonProperty("final_drive_ratio")
    private double finalDriveRatio;

    @JsonProperty("anti_roll_force")
    private double antiRollForce;

    @JsonProperty("steering_helper_strength")
    private double steerHelperStrength;

    @JsonProperty("traction_helper_strength")
    private double tractionHelperStrength;

    @JsonProperty("is_enabled")
    private boolean isEnabled;

    @JsonProperty("last_modified_date")
    private String lastModifiedDate;

    @JsonProperty("created_date")
    private String createdDate;

}
