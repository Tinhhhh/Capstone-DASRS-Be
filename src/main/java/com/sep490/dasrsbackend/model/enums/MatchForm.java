package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum MatchForm {
    OFFICIAL("OFFICIAL"),
    REMATCH("REMATCH");

    private final String status;

    MatchForm(String status) {
        this.status = status;
    }
}
