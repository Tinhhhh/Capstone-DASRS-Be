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

    @JsonProperty("tournament_name")
    @NotBlank(message = "Tournament name is required")
    @Size(max = 200, message = "Tournament name no more than 200 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ0-9\\s-_!,.:\"']+$", message = "Tournament name contains invalid characters")
    private String tournamentName;

    @JsonProperty("tournament_context")
    @NotBlank(message = "Tournament context is required")
    @Size(max = 200, message = "Tournament name no more than 5000 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ0-9\\s-_!,.:\"']+$", message = "Tournament context contains invalid characters")
    private String tournamentContext;

    @Min(value = 2, message = "Team number must be at least 2")
    @Max(value = 20, message = "Team number must be at most 20")
    @JsonProperty("team_number")
    private int teamNumber;

    @JsonProperty("start_date")
    private Date startDate;

    @JsonProperty("end_date")
    private Date endDate;

}
