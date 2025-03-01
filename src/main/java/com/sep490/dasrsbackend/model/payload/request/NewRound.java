package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Tournament id is required")
    private Long tournamentId;

    @JsonProperty("scored_method_id")
    @NotBlank(message = "ScoredMethodId id is required")
    private Long scoredMethodId;

    @JsonProperty("environment_id")
    @NotBlank(message = "EnvironmentId id is required")
    private Long environmentId;

    @JsonProperty("match_type_id")
    @NotBlank(message = "MatchTypeId id is required")
    private Long matchTypeId;
}
