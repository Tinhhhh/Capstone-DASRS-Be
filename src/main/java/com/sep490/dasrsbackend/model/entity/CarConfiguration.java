package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "car_configuration")
@EntityListeners(AuditingEntityListener.class)
public class CarConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_configuration_id")
    @JsonProperty("car_configuration_id")
    private Long carConfigurationId;

    @JsonProperty("car_configuration_name")
    @Column(name = "car_configuration_name")
    private String carConfigurationName;

    @Column(name = "friction")
    private double Friction;

    @Column(name = "acceleration")
    private double acceleration;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @LastModifiedDate
    @JsonProperty("last_modified_date")
    @Column(name = "last_modified_date", insertable = false)
    private Date lastModifiedDate;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
