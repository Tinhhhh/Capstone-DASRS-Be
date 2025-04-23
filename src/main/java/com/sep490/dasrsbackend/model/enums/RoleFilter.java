package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum RoleFilter {
    PLAYER("PLAYER"),
    ORGANIZER("ORGANIZER"),
    ALL("ALL");

    private final String role;

    RoleFilter(String role) {
        this.role = role;
    }
}
