package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum RoundStatusFilter {
    ACTIVE("ACTIVE"),
    TERMINATED("TERMINATED"),
    COMPLETED("COMPLETED"),
    ALL("ALL");

    private final String status;

    RoundStatusFilter(String status) {
        this.status = status;
    }

}
