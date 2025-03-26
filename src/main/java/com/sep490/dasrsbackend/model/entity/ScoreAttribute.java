package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalTime;
import java.util.List;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_attribute_id")
    private Long id;


    @Column(name = "lap")
    private int lap;

    @JsonFormat(pattern = "mm:ss.SSS")
    @Column(name = "fastest_lap_time")
    private LocalTime fastestLapTime;

    @JsonProperty("collision")
    @Column(name = "collision")
    private int collision;

    @JsonFormat(pattern = "mm:ss.SSS")
    @Column(name = "total_race_time")
    private LocalTime totalRaceTime;

    @Column(name = "off_track")
    private int offTrack;

    @Column(name = "assist_usage")
    private int assistUsageCount;

    @Column(name = "top_speed")
    private double topSpeed;

    @Column(name = "average_speed")
    private double averageSpeed;

    @Column(name = "total_distance")
    private double totalDistance;

    @OneToMany(mappedBy = "scoreAttribute")
    private List<MatchTeam> matchAccounts;
}
