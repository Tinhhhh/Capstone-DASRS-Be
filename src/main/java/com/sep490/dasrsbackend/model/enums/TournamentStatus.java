package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum TournamentStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String status;

    TournamentStatus(String status) {
        this.status = status;
    }

}
