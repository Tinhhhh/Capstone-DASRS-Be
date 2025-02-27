package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    ADMIN_CREATE_ACCOUNT("ADMIN_CREATE_ACCOUNT");

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
