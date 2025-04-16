package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnityRoomResponse {

    @JsonProperty(value = "is_success", index = 1)
    private boolean isSuccess;

    @JsonProperty(value = "scored_method_id", index = 2)
    private Long scoredMethodId;

    @JsonProperty(value = "match_code", index = 3)
    private String matchCode;

    @JsonProperty("message")
    private String message;
}
