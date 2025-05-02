package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.EnvironmentStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditEnvironment {

    @JsonProperty("environment_name")
    @Size(max = 200, message = "Environment name no more than 200 characters")
    private String name;

    @JsonProperty("status")
    private EnvironmentStatus status;
}
