package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.enums.ScoredMethodStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoredMethodResponse {

    @JsonProperty("scored_method_id")
    private Long scoreId;

    private double lap;

    @JsonProperty("fastest_lap_time")
    private double fastestLapTime;

    @JsonProperty("collision")
    private double collision;

    @JsonProperty("total_race_time")
    private double totalRaceTime;

    @JsonProperty("off_track")
    private double offTrack;

    @JsonProperty("assist_usage")
    private double assistUsageCount;

    @JsonProperty("top_speed")
    private double topSpeed;

    @JsonProperty("average_speed")
    private double averageSpeed;

    @JsonProperty("total_distance")
    private double totalDistance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ScoredMethodStatus status;

    @LastModifiedDate
    @JsonProperty("last_modified_date")
    private Date lastModifiedDate;

}
