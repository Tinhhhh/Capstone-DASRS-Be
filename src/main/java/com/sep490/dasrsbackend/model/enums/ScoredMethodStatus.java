package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum ScoredMethodStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String status;

    ScoredMethodStatus(String status) {
        this.status = status;
    }
}
