package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum ReviewStatus {
    PENDING("PENDING"),
    FINISHED("FINISHED"),
    CANCELLED("CANCELLED");

    private final String status;

    ReviewStatus(String status) {
        this.status = status;
    }
}
