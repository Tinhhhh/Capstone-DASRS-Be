package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;

@Getter
public enum RecordStatus {
    SUCCESS("SUCCESS"),
    ERROR("ERROR");

    private final String status;

    RecordStatus(String status) {
        this.status = status;
    }
}
