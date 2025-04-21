package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"roundId", "roundName", "complaints"})
public class RoundComplaintResponse {

    @JsonProperty("round_id")
    private Long roundId;

    @JsonProperty("round_name")
    private String roundName;

    private List<ComplaintResponseDetails> complaints;
}
