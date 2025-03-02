package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.ScoreAttributeStatus;
import com.sep490.dasrsbackend.model.enums.ScoredMethodStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "scored_method")
@EntityListeners(AuditingEntityListener.class)
public class ScoredMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scored_method_id")
    private Long id;

    @Column(name = "lap")
    private double lap;

    @Column(name = "fastest_lap_time")
    private double fastestLapTime;

    @Column(name = "collision")
    private double collision;

    @Column(name = "total_race_time")
    private double totalRaceTime;

    @Column(name = "off_track")
    private double offTrack;

    @Column(name = "assist_usage")
    private double assistUsageCount;

    @Column(name = "top_speed")
    private double topSpeed;

    @Column(name = "average_speed")
    private double averageSpeed;

    @Column(name = "total_distance")
    private double totalDistance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ScoredMethodStatus status;

    @LastModifiedDate
    @Column(name = "last_modified_date",  nullable = false)
    private Date lastModifiedDate;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "scoredMethod")
    private List<Round> roundList;
}
