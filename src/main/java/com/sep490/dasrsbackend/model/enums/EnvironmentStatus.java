package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum EnvironmentStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String status;

    EnvironmentStatus(String status) {
        this.status = status;
    }
}
