package com.sep490.dasrsbackend.model.payload.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamResponse {
    private UUID id;
    private String fullName;
}
