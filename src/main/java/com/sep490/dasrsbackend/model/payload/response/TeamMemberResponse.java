package com.sep490.dasrsbackend.model.payload.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {
    private UUID id; // Account ID
    private String fullName; // Full name of the member
}


