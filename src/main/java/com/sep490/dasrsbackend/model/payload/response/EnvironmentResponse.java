package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.EnvironmentStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentResponse {

    @JsonProperty("environment_id")
    private Long id;

    @JsonProperty("environment_name")
    private String name;

    private EnvironmentStatus status;

}
