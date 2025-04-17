package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewRound {

    @JsonProperty(index = 1)
    @NotBlank(message = "Tournament context is required")
    @Size(max = 5000, message = "Tournament name no more than 5000 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ0-9\\s-_!,.]+$", message = "Tournament context contains invalid characters")
    private String description;

    @JsonProperty(value = "round_name", index = 2)
    @Size(max = 200, message = "Tournament name no more than 200 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ0-9\\s-_!,.]+$", message = "Tournament context contains invalid characters")
    private String roundName;

    @JsonProperty(value = "round_duration", index = 3)
    @Min(value = 0, message = "Round duration must be equal or greater than 0")
    @Max(value = 300, message = "Round duration must be less than 300")
    private int roundDuration;

    @JsonProperty(value = "lap_number", index = 4)
    @Min(value = 0, message = "Lap number must be equal or greater than 0")
    @Max(value = 3, message = "Lap number must be less than 3")
    private int lapNumber;

    @JsonProperty(value = "finish_type", index = 5)
    private FinishType finishType;

    @JsonProperty(value = "team_limit", index = 6)
    @Min(value = 0, message = "Team limit must be equal or greater than 0")
    @Max(value = 19, message = "Team limit must be less than 20")
    @NotNull(message = "Team limit is required")
    private int teamLimit;

    @JsonProperty(value = "is_last", index = 7)
    private boolean isLast;

    @JsonProperty(value = "start_date", index = 8)
    private Date startDate;

    @JsonProperty(value = "end_date", index = 9)
    private Date endDate;

    @JsonProperty(value = "tournament_id", index = 10)
    private Long tournamentId;

    @JsonProperty(value = "environment_id", index = 11)
    private Long environmentId;

    @JsonProperty(value = "match_type_id", index = 12)
    private Long matchTypeId;

    @JsonProperty(value = "resource_id", index = 13)
    private Long resourceId;

    @JsonProperty(value = "lap", index = 14)
    @DecimalMax(value = "500.0", message = "Maximum value can be bonus is 500.0")
    @DecimalMin(value = "0.0", message = "Maximum value can be deducted is 0.0")
    private double lap;

    @DecimalMax(value = "0.0", message = "Maximum value can be bonus is 0.0")
    @DecimalMin(value = "-50.0", message = "Maximum value can be deducted is -50.0")
    @JsonProperty(value = "collision", index = 15)
    private double collision;

    @DecimalMax(value = "0.0", message = "Maximum value can be bonus is 0.0")
    @DecimalMin(value = "-10.0", message = "Maximum value can be deducted is -10.0")
    @JsonProperty(value = "total_race_time", index = 16)
    private double totalRaceTime;

    @DecimalMax(value = "0.0", message = "Maximum value can be bonus is 0.0")
    @DecimalMin(value = "-10.0", message = "Maximum value can be deducted is -10.0")
    @JsonProperty(value = "off_track", index = 17)
    private double offTrack;

    @DecimalMax(value = "0.0", message = "Maximum value can be bonus is 0.0")
    @DecimalMin(value = "-500.0", message = "Maximum value can be deducted is -500.0")
    @JsonProperty(value = "assist_usage ", index = 18)
    private double assistUsageCount;

    @DecimalMax(value = "30.0", message = "Maximum value can be bonus is 30.0")
    @DecimalMin(value = "0.0", message = "Maximum value can be deducted is 0.0")
    @JsonProperty(value = "average_speed", index = 19)
    private double averageSpeed;

    @DecimalMax(value = "100.0", message = "Maximum value can be bonus is 100.0")
    @DecimalMin(value = "0.0", message = "Maximum value can be deducted is 0.0")
    @JsonProperty(value = "total_distance", index = 20)
    private double totalDistance;

}
