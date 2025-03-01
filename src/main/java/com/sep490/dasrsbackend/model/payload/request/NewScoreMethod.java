package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewScoreMethod {

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
}
