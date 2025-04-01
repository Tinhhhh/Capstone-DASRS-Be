package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @JsonProperty("team_limit")
    private int teamLimit;

    @JsonProperty("is_last")
    private boolean isLast;

    private String description;

    private RoundStatus status;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("created_date")
    private String createDate;

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

    @JsonProperty("map_id")
    private Long mapId;

    @JsonProperty("finish_type")
    private FinishType finishType;
}
