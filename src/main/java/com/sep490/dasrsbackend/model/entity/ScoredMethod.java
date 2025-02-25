package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.ScoreAttributeStatus;
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
@Table(name = "score_attribute")
@EntityListeners(AuditingEntityListener.class)
public class ScoreAttribute {
    @Id
    @GeneratedValue
    @Column(name = "score_attribute_id")
    @JsonProperty("score_attribute_id")
    private Long scoreId;

    @JsonProperty("lap_count")
    @Column(name = "lap_count")
    private String lapCount;

    @JsonProperty("fastest_lap_time")
    @Column(name = "fastest_lap_time")
    private String fastestLapTime;

    @JsonProperty("collision_count")
    @Column(name = "collision_count")
    private String collisionCount;

    @JsonProperty("total_race_time")
    @Column(name = "total_race_time")
    private String totalRaceTime;

    @JsonProperty("off_track_count")
    @Column(name = "off_track_count")
    private String offTrackCount;

    @JsonProperty("assist_usage_count")
    @Column(name = "assist_usage_count")
    private String assistUsageCount;

    @JsonProperty("top_speed")
    @Column(name = "top_speed")
    private String topSpeed;

    @JsonProperty("average_speed")
    @Column(name = "average_speed")
    private String averageSpeed;

    @JsonProperty("total_distance")
    @Column(name = "total_distance")
    private String totalDistance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ScoreAttributeStatus status;

    @LastModifiedDate
    @JsonProperty("last_modified_date")
    @Column(name = "last_modified_date", insertable = false)
    private Date lastModifiedDate;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;
}
