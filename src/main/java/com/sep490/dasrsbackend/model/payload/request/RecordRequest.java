package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordRequest {

    @JsonProperty("record_link")
    @NotBlank(message = "Record link is required")
    @Size(max = 500, message = "Record link must not exceed 500 characters")
    private String recordLink;

    @JsonProperty("status")
    @NotNull(message = "Record status is required")
    private RecordStatus status;

    @JsonProperty("match_id")
    @NotNull(message = "Match ID is required")
    private Long matchId;
}
