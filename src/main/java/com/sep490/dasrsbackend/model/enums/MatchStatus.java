package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum MatchStatus {
    PENDING("PENDING"),
    FINISHED("FINISHED"),
    TERMINATED("TERMINATED");

    private final String status;

    MatchStatus(String status) {
        this.status = status;
    }
}
