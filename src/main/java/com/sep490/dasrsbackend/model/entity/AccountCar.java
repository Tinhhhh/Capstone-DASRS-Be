package com.sep490.dasrsbackend.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "account_car")
public class AccountCar {

    @EmbeddedId
    private AccountCarId id;

    @Column(name = "car_name")
    private String carName;

    @Column(name = "front_camper")
    private Double frontCamper;

    @Column(name = "rear_camper")
    private Double rearCamper;

    @Column(name = "front_ssr")
    private Double frontSSR;

    @Column(name = "rear_ssr")
    private Double rearSSR;

    @Column(name = "front_suspension")
    private Double frontSuspension;

    @Column(name = "rear_suspension")
    private Double rearSuspension;

    @Column(name = "front_ssd")
    private Double frontSSD;

    @Column(name = "rear_ssd")
    private Double rearSSD;

    @Column(name = "engine")
    private Double engine;

    @Column(name = "handling")
    private Double handling;

    @Column(name = "brake")
    private Double brake;

    @ManyToOne
    @MapsId("accountId")
    @JoinColumn(name = "account_Id")
    Account account;

    @ManyToOne
    @MapsId("carId")
    @JoinColumn(name = "car_Id")
    Car car;


}
