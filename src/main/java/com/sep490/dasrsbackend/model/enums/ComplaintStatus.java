package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum ComplaintStatus {
    PENDING("PENDING"),
    FINISHED("FINISHED"),
    CANCELLED("CANCELLED");

    private final String status;

    ComplaintStatus(String status) {
        this.status = status;
    }
}
