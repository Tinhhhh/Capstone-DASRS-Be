package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundResponse {

    @JsonProperty("round_id")
    private Long id;

    @JsonProperty("round_name")
    private String roundName;

    private String description;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("tournament_id")
    private Long tournamentId;

    @JsonProperty("scored_method_id")
    private Long scoredMethodId;

    @JsonProperty("environment_id")
    private Long environmentId;

    @JsonProperty("match_type_id")
    private Long matchTypeId;

    @JsonProperty("match_type_name")
    private String matchTypeName;

    @JsonProperty("finish_type")
    private FinishType finishType;
}
