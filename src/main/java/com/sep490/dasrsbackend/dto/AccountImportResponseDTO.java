package com.sep490.dasrsbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AccountImportResponseDTO {

    @JsonProperty("account_id")
    private UUID accountId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;

    private String address;

    private String gender;

    private LocalDate dob;

    private String phone;

    private String avatar;

    @JsonProperty("is_locked")
    private boolean isLocked;

    @JsonProperty("is_leader")
    private boolean isLeader;

    @JsonProperty("role_name")
    private String roleName;

    @JsonProperty("team_name")
    private String teamName;
}
