package com.sep490.dasrsbackend.model.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponse {
    private UUID accountId;
    private String lastName;
    private String firstName;
    private String email;
    private String gender;
    private String phone;
    private String avatar;
    private boolean isLeader;
    private Long teamId;
    private String teamName;
}
