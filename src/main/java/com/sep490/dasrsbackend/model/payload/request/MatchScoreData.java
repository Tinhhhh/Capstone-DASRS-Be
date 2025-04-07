package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchScoreData {

    @JsonProperty("match_code")
    private String matchCode;

    @JsonProperty("player_id")
    private UUID playerId;

    @Min(value = 0, message = "Lap number must be at least 0")
    @JsonProperty("lap")
    private int lap;

    @PositiveOrZero
    @JsonProperty("fastest_lap_time")
    private double fastestLapTime;

    @Min(value = 0, message = "Collision number must be at least 0")
    @JsonProperty("collision")
    private int collision;

    @PositiveOrZero
    @JsonProperty("total_race_time")
    private double totalRaceTime;

    @Min(value = 0, message = "Off track number must be at least 0")
    @JsonProperty("off_track")
    private int offTrack;

    @Min(value = 0, message = "Assist usage number must be at least 0")
    @JsonProperty("assist_usage")
    private int assistUsageCount;

    @PositiveOrZero
    @JsonProperty("top_speed")
    private double topSpeed;

    @PositiveOrZero
    @JsonProperty("average_speed")
    private double averageSpeed;

    @PositiveOrZero
    @JsonProperty("total_distance")
    private double totalDistance;

}
