package com.sep490.dasrsbackend.model.payload.response;

import com.sep490.dasrsbackend.model.enums.TeamStatus;
import lombok.*;

import java.util.UUID;

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
}

