package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum RoundStatus {
    ACTIVE("ACTIVE"),
    TERMINATED("TERMINATED"),
    SUSPENDED("SUSPENDED"),
    COMPLETED("COMPLETED");

    private final String status;

    RoundStatus(String status) {
        this.status = status;
    }
}
