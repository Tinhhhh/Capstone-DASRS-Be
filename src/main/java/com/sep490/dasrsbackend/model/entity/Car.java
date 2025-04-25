package com.sep490.dasrsbackend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "car")
@EntityListeners(AuditingEntityListener.class)
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Long id;

    @Column(name = "car_name", nullable = false)
    private String carName;

    @Column(name = "maximum_torque", nullable = false)
    private double maxTorqueAsNM;

    @Column(name = "minimum_engine_rpm", nullable = false)
    private double minEngineRPM;

    @Column(name = "maximum_engine_rpm", nullable = false)
    private double maxEngineRPM;

    @Column(name = "shift_up_rpm", nullable = false)
    private double shiftUpRPM;

    @Column(name = "shift_down_rpm", nullable = false)
    private double shiftDownRPM;

    @Column(name = "final_drive_ratio", nullable = false)
    private double finalDriveRatio;

    @Column(name = "anti_roll_force", nullable = false)
    private double antiRollForce;

    @Column(name = "steering_helper_strength", nullable = false)
    private double steerHelperStrength;

    @Column(name = "traction_helper_strength", nullable = false)
    private double tractionHelperStrength;

    @Column(name = "max_brake_torque", nullable = false)
    private double maxBrakeTorque;

    @Column(name = "front_camper", nullable = false)
    private double frontCamper;

    @Column(name = "rear_camper", nullable = false)
    private double rearCamper;

    @Column(name = "front_ssr", nullable = false)
    private double frontSSR;

    @Column(name = "rear_ssr", nullable = false)
    private double rearSSR;

    @Column(name = "front_suspension", nullable = false)
    private double frontSuspension;

    @Column(name = "rear_suspension", nullable = false)
    private double rearSuspension;

    @Column(name = "front_ssd", nullable = false)
    private double frontSSD;

    @Column(name = "rear_ssd", nullable = false)
    private double rearSSD;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "car")
    private List<MatchTeam> matchTeams;
}
