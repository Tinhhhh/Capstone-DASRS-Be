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
public class NewTournament {

    @JsonProperty("tournament_name")
    @NotBlank(message = "Tournament name is required")
    @Size(max = 200, message = "Tournament name no more than 200 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s-_]+$", message = "Tournament context contains invalid characters")
    private String tournamentName;

    @JsonProperty("tournament_context")
    @NotBlank(message = "Tournament context is required")
    @Size(max = 5000, message = "Tournament name no more than 5000 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s-_!,]+$", message = "Tournament context contains invalid characters")
    private String tournamentContext;

    @Size(min = 10, max = 20, message = "Team number must be between 10 and 20")
    private int teamNumber;

    @Future(message = "Start date must be in the future")
    @JsonProperty("start_date")
    private Date startDate;

    @Future(message = "End date must be in the future")
    @JsonProperty("end_date")
    private Date endDate;



}
