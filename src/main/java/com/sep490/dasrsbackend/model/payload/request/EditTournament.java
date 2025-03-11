package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;


import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditTournament {

    @JsonProperty("tournament_context")
    @Size(max = 5000, message = "Tournament name no more than 5000 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ0-9\\s-_!,.:\"']+$", message = "Tournament context contains invalid characters")
    private String tournamentContext;

    @Min(value = 10, message = "Team number must be at least 10")
    @Max(value = 20, message = "Team number must be at most 20")
    @JsonProperty("team_number")
    private int teamNumber;

    @Future(message = "Start date must be in the future")
    @JsonProperty("start_date")
    private Date startDate;

    @Future(message = "End date must be in the future")
    @JsonProperty("end_date")
    private Date endDate;
}
