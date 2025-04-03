package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewCar {

    @JsonProperty("car_name")
    private String carName;

    @JsonProperty("maximum_torque")
    @DecimalMax(value = "1000.0", message = "maximum_torque must be less than or equal to 1000")
    @DecimalMin(value = "100.0", message = "maximum_torque must be greater than or equal to 100")
    private double maxTorqueAsNM;

    @JsonProperty("minimum_engine_rpm")
    @DecimalMax(value = "2000.0", message = "minimum_engine_rpm must be less than or equal to 2000")
    @DecimalMin(value = "200.0", message = "minimum_engine_rpm must be greater than or equal to 200")
    private double minEngineRPM;

    @JsonProperty("maximum_engine_rpm")
    @DecimalMax(value = "10000.0", message = "maximum_engine_rpm must be less than or equal to 10000")
    @DecimalMin(value = "500.0", message = "maximum_engine_rpm must be greater than or equal to 500")
    private double maxEngineRPM;

    @JsonProperty("shift_up_rpm")
    @DecimalMax(value = "10000.0", message = "shift_up_rpm must be less than or equal to 10000")
    @DecimalMin(value = "1000.0", message = "shift_up_rpm must be greater than or equal to 1000")
    private double shiftUpRPM;

    @JsonProperty("shift_down_rpm")
    @DecimalMax(value = "10000.0", message = "shift_down_rpm must be less than or equal to 10000")
    @DecimalMin(value = "1000.0", message = "shift_down_rpm must be greater than or equal to 1000")
    private double shiftDownRPM;

    @JsonProperty("final_drive_ratio")
    @DecimalMin(value = "1.0", message = "final_drive_ratio must be greater than or equal to 1")
    @DecimalMax(value = "5.0", message = "final_drive_ratio must be less than or equal to 5")
    private double finalDriveRatio;

    @JsonProperty("anti_roll_force")
    @DecimalMax(value = "2000.0", message = "anti_roll_force must be less than or equal to 2000")
    @DecimalMin(value = "100.0", message = "anti_roll_force must be greater than or equal to 100")
    private double antiRollForce;

    @JsonProperty("steering_helper_strength")
    @DecimalMax(value = "1.0", message = "steering_helper_strength must be less than or equal to 1")
    @DecimalMin(value = "0.0", message = "steering_helper_strength must be greater than or equal to 0")
    private double steerHelperStrength;

    @JsonProperty("traction_helper_strength")
    @DecimalMin(value = "0.0", message = "traction_helper_strength must be greater than or equal to 0")
    @DecimalMax(value = "1.0", message = "traction_helper_strength must be less than or equal to 1")
    private double tractionHelperStrength;

}
