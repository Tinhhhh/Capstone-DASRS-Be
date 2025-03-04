package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import com.sep490.dasrsbackend.model.enums.MatchTypeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchTypeResponse {
    @JsonProperty("match_type_id")
    private Long id;

    @JsonProperty("match_type_name")
    private String matchTypeName;

    @JsonProperty("match_type_code")
    private String matchTypeCode;

    @JsonProperty("match_duration")
    private double matchDuration;

    @JsonProperty("finish_type")
    private FinishType finishType;

    private MatchTypeStatus status;
}
