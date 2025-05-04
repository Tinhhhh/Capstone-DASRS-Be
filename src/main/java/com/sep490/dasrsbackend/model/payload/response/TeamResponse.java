package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.TeamStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {

    private Long id;

    private String name;

    private String tag;

    private boolean disqualified;

    private TeamStatus status;

    @JsonProperty("member_count")
    private int memberCount;
}

