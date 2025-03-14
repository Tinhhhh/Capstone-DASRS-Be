package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.ScoreAttributeStatus;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDataRequest {

    @JsonProperty("team_id")
    private Long teamId;

    @JsonProperty("match_id")
    private Long matchId;

    @JsonProperty("player_id")
    private UUID playerId;

    @JsonProperty("lap")
    private int lap;

    @JsonProperty("fastest_lap_time")
    private LocalTime fastestLapTime;

    @JsonProperty("collision")
    private int collision;

    @JsonProperty("total_race_time")
    private LocalTime totalRaceTime;

    @JsonProperty("off_track")
    private int offTrack;

    @JsonProperty("assist_usage")
    private int assistUsageCount;

    @JsonProperty("top_speed")
    private double topSpeed;

    @JsonProperty("average_speed")
    private double averageSpeed;

    @JsonProperty("total_distance")
    private double totalDistance;

    @JsonProperty("status")
    private ScoreAttributeStatus status;

}
