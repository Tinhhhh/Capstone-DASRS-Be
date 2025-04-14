package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("lap_score")
    private double lapScore;

    @JsonProperty("lap")
    private int lap;

    @JsonProperty("fastest_lap_time")
    private double fastestLapTime;

    @JsonProperty("collision_score")
    private double collisionScore;

    @JsonProperty("collision")
    private int collision;

    @JsonProperty("total_race_time_scored")
    private double totalRaceTimeScore;

    @JsonProperty("total_race_time")
    private double totalRaceTime;

    @JsonProperty("off_track_score")
    private double offTrackScore;

    @JsonProperty("off_track")
    private int offTrack;

    @JsonProperty("assist_usage_score")
    private double assistUsageScore;

    @JsonProperty("assist_usage")
    private int assistUsageCount;

    @JsonProperty("top_speed")
    private double topSpeed;

    @JsonProperty("average_speed_score")
    private double averageSpeedScore;

    @JsonProperty("average_speed")
    private double averageSpeed;

    @JsonProperty("total_distance_score")
    private double totalDistanceScore;

    @JsonProperty("total_distance")
    private double totalDistance;

}
