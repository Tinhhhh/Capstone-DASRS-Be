package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum ResourceType {
    UI("UI"),
    MAP("MAP");

    private final String type;

    ResourceType(String type) {
        this.type = type;
    }

}
