package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.RecordStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordResponse {

    @JsonProperty("record_id")
    private Long recordId;

    @JsonProperty("record_link")
    private String recordLink;

    @JsonProperty("status")
    private RecordStatus status;

    @JsonProperty("match_id")
    private Long matchId;
}
