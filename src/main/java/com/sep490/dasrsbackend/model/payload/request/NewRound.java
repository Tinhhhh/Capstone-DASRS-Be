package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
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
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s-_]+$", message = "Tournament context contains invalid characters")
    private String roundName;

    @JsonProperty("team_limit")
    @Min(value = 0, message = "Team limit must be equal or greater than 0")
    @Max(value = 19, message = "Team limit must be less than 20")
    @NotNull(message = "Team limit is required")
    private int teamLimit;

    @JsonProperty("is_last")
    private boolean isLast;

    @NotBlank(message = "Tournament context is required")
    @Size(max = 5000, message = "Tournament name no more than 5000 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s-_!,]+$", message = "Tournament context contains invalid characters")
    private String description;

    @Future(message = "Start date must be in the future")
    @JsonProperty("start_date")
    private Date startDate;

    @Future(message = "End date must be in the future")
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
