package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum TeamStatus {
    ACTIVE("ACTIVE"),
    COMPLETED("COMPLETED"),
    TERMINATED("TERMINATED");

    private final String status;

    TeamStatus(String status) {
        this.status = status;
    }
}
