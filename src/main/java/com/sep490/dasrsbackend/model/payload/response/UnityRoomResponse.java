package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnityRoomResponse {

    @JsonProperty("is_success")
    private boolean isSuccess;

    @JsonProperty("message")
    private String message;
}
