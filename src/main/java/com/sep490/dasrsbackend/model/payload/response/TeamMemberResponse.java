package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("is_leader")
    private boolean isLeader;
}


