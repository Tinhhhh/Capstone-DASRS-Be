package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum ScoreAttributeStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String status;

    ScoreAttributeStatus(String status) {
        this.status = status;
    }
}
