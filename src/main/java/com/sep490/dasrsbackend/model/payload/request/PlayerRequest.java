package com.sep490.dasrsbackend.model.payload.request;

import lombok.Data;

@Data
public class PlayerRequest {
    private String teamName;
    private Boolean isLeader;
}
