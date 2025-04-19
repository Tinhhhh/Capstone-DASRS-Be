package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum ComplaintStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String status;

    ComplaintStatus(String status) {
        this.status = status;
    }
}
