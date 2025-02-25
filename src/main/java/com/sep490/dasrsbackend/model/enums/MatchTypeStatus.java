package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum MatchTypeStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String name;

    MatchTypeStatus(String name) {
        this.name = name;
    }
}
