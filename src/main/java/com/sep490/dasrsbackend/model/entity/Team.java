package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "car")
@EntityListeners(AuditingEntityListener.class)
public class Car {
    @Id
    @GeneratedValue
    @Column(name = "car_id")
    @JsonProperty("car_id")
    private Long roleId;

    @JsonProperty("car_name")
    @Column(name = "car_name")
    private String roleName;

    @JsonProperty("last_modified_date")
    @Column(name = "last_modified_date", insertable = false)
    private Date lastModifiedDate;
}
