package com.sep490.dasrsbackend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @Column(name = "lap", nullable = false)
    private int lap;

    @Column(name = "fastest_lap_time", nullable = false)
    private double fastestLapTime;

    @Column(name = "collision", nullable = false)
    private int collision;

    @Column(name = "total_race_time", nullable = false)
    private double totalRaceTime;

    @Column(name = "off_track", nullable = false)
    private int offTrack;

    @Column(name = "assist_usage", nullable = false)
    private int assistUsageCount;

    @Column(name = "top_speed", nullable = false)
    private double topSpeed;

    @Column(name = "average_speed", nullable = false)
    private double averageSpeed;

    @Column(name = "total_distance", nullable = false)
    private double totalDistance;

    @OneToMany(mappedBy = "scoreAttribute")
    private List<MatchTeam> matchAccounts;
}
