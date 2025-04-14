package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum TournamentStatusFilter {
    ACTIVE("ACTIVE"),
    TERMINATED("TERMINATED"),
    COMPLETED("COMPLETED"),
    ALL("ALL");

    private final String status;

    TournamentStatusFilter(String status) {
        this.status = status;
    }
}
