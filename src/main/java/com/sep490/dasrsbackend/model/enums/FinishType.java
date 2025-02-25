package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum FinishType {
    LAP("LAP"),
    TIME("TIME");

    private final String name;

    FinishType(String name) {
        this.name = name;
    }
}
