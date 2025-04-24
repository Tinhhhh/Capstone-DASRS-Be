package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewEnvironment {

    @JsonProperty("environment_name")
    @Size(max = 200, message = "Environment name no more than 200 characters")
    private String name;

}
