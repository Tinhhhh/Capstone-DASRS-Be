package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    PLAYER("PLAYER"),
    STAFF("STAFF"),
    ADMIN("ADMIN"),
    ORGANIZER("ORGANIZER");

    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }

}
