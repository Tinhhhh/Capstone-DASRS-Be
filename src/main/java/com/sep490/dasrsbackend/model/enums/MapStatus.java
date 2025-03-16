package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum MapStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String name;

    MapStatus(String name) {
        this.name = name;
    }
}
