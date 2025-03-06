package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import com.sep490.dasrsbackend.model.enums.MatchTypeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewMatchType {

    @JsonProperty("match_type_name")
    @Size(max = 200, message = "Match type name no more than 200 characters")
    private String matchTypeName;

    @JsonProperty("match_type_code")
    @Pattern(regexp = "^[A-Za-z]{2,3}-[1-5]{2}[A-Za-z]{1}$", message = "Match type code contains few letters and numbers, example: AB-12")
    private String matchTypeCode;

    @JsonProperty("match_duration")
    @DecimalMin(value = "0.5", message = "matchDuration must be at least 0.5")
    @DecimalMax(value = "1", message = "matchDuration must not exceed 1")
    private double matchDuration;

    @JsonProperty("finish_type")
    private FinishType finishType;

}
