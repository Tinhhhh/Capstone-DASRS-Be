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

    @JsonProperty("round_name")
    @Size(max = 200, message = "Tournament name no more than 200 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ0-9\\s-_!,.]+$", message = "Tournament context contains invalid characters")
    private String roundName;

    @JsonProperty("round_duration")
    @Min(value = 0, message = "Round duration must be equal or greater than 0")
    @Max(value = 300, message = "Round duration must be less than 300")
    private int roundDuration;

    @JsonProperty("lap_number")
    @Min(value = 0, message = "Lap number must be equal or greater than 0")
    @Max(value = 3, message = "Lap number must be less than 3")
    private int lapNumber;

    @JsonProperty("finish_type")
    private FinishType finishType;

    @JsonProperty("team_limit")
    @Min(value = 0, message = "Team limit must be equal or greater than 0")
    @Max(value = 19, message = "Team limit must be less than 20")
    @NotNull(message = "Team limit is required")
    private int teamLimit;

    @JsonProperty("is_last")
    private boolean isLast;

    @NotBlank(message = "Tournament context is required")
    @Size(max = 5000, message = "Tournament name no more than 5000 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ0-9\\s-_!,.]+$", message = "Tournament context contains invalid characters")
    private String description;

    @JsonProperty("start_date")
    private Date startDate;

    @JsonProperty("end_date")
    private Date endDate;

    @JsonProperty("tournament_id")
    private Long tournamentId;

    @JsonProperty("scored_method_id")
    private Long scoredMethodId;

    @JsonProperty("environment_id")
    private Long environmentId;

    @JsonProperty("match_type_id")
    private Long matchTypeId;

    @JsonProperty("resource_id")
    private Long resourceId;
}
