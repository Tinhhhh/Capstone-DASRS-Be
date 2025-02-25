package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.ScoreAttributeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalTime;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "score_attribute")
@EntityListeners(AuditingEntityListener.class)
public class ScoreAttribute {
    @Id
    @GeneratedValue
    @Column(name = "score_attribute_id")
    @JsonProperty("score_attribute_id")
    private Long scoreId;


    @Column(name = "lap")
    private int lap;

    @JsonFormat(pattern = "mm:ss.SSS")
    @JsonProperty("fastest_lap_time")
    @Column(name = "fastest_lap_time")
    private LocalTime fastestLapTime;

    @JsonProperty("collision")
    @Column(name = "collision")
    private int collision;

    @JsonFormat(pattern = "mm:ss.SSS")
    @JsonProperty("total_race_time")
    @Column(name = "total_race_time")
    private LocalTime totalRaceTime;

    @JsonProperty("off_track")
    @Column(name = "off_track")
    private int offTrack;

    @JsonProperty("assist_usage")
    @Column(name = "assist_usage")
    private int assistUsageCount;

    @JsonProperty("top_speed")
    @Column(name = "top_speed")
    private double topSpeed;

    @JsonProperty("average_speed")
    @Column(name = "average_speed")
    private double averageSpeed;

    @JsonProperty("total_distance")
    @Column(name = "total_distance")
    private double totalDistance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ScoreAttributeStatus status;

    @OneToMany(mappedBy = "scoreAttribute")
    private Set<Match> match;
}
