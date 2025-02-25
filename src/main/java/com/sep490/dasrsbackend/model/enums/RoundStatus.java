package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum RoundStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String status;

    RoundStatus(String status) {
        this.status = status;
    }
}
