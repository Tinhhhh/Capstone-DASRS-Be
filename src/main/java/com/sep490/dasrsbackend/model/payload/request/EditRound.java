package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
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
public class EditRound {

    @JsonProperty("round_id")
    private Long id;

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

    @JsonProperty("scored_method_id")
    private Long scoredMethodId;

    @JsonProperty("environment_id")
    private Long environmentId;

    @JsonProperty("match_type_id")
    private Long matchTypeId;

    @JsonProperty("status")
    private RoundStatus status;
}

