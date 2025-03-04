package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentException {

    @JsonProperty(index = 1, value = "start_date")
    private String startDate;

    @JsonProperty(index = 2, value = "end_date")
    private String endDate;

    @JsonProperty(index = 3, value = "type")
    private String type;
}
