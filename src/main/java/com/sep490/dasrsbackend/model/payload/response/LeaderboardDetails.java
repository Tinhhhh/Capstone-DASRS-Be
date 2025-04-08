package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardDetails {

    @JsonProperty("player_id")
    private UUID playerId;

    @JsonProperty("team_id")
    private Long teamId;

    @JsonProperty("player_name")
    private String playerName;

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("ranking")
    private int ranking;

    @JsonProperty("team_score")
    private double score;

    @JsonProperty("lap")
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
