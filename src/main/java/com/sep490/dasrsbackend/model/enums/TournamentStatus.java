package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum TournamentStatus {
    PENDING("PENDING"),
    ACTIVE("ACTIVE"),
    TERMINATED("TERMINATED"),
    COMPLETED("COMPLETED");

    private final String status;

    TournamentStatus(String status) {
        this.status = status;
    }

}
