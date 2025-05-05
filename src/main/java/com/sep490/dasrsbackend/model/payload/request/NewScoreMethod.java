package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewScoreMethod {

    @DecimalMax(value = "500.0", message = "Team number must be at most 500.0")
    @DecimalMin(value = "0.0", message = "Team number must be at least 0.0")
    private double lap;

    @DecimalMax(value = "0.0", message = "Team number must be at most 0.0")
    @DecimalMin(value = "-10.0", message = "Team number must be at least -10.0")
    @JsonProperty("collision")
    private double collision;

    @DecimalMax(value = "0.0", message = "Team number must be at most 0.0")
    @DecimalMin(value = "-10.0", message = "Team number must be at least -10.0")
    @JsonProperty("total_race_time")
    private double totalRaceTime;

    @DecimalMax(value = "0.0", message = "Team number must be at most 0.0")
    @DecimalMin(value = "-10.0", message = "Team number must be at least -10.0")
    @JsonProperty("off_track")
    private double offTrack;

    @DecimalMax(value = "0.0", message = "Team number must be at most 0.0")
    @DecimalMin(value = "-100.0", message = "Team number must be at least -100.0")
    private double assistUsageCount;

    @DecimalMax(value = "10.0", message = "Team number must be at most 10.0")
    @DecimalMin(value = "1.0", message = "Team number must be at least 1.0")
    @JsonProperty("average_speed")
    private double averageSpeed;

    @DecimalMax(value = "100.0", message = "Team number must be at most 100.0")
    @DecimalMin(value = "1.0", message = "Team number must be at least 1.0")
    @JsonProperty("total_distance")
    private double totalDistance;

    @JsonProperty("match_finish_type")
    private FinishType matchFinishType;
}
