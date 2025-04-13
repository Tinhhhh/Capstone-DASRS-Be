package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum MatchTeamStatus {
    ASSIGNED("ASSIGNED"),
    UNASSIGNED("UNASSIGNED"),
    TERMINATED("TERMINATED");

    private final String status;

    MatchTeamStatus(String status) {
        this.status = status;
    }
}
