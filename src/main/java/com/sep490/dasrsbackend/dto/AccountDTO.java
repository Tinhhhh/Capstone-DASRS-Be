package com.sep490.dasrsbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.entity.Role;
import com.sep490.dasrsbackend.model.entity.Team;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AccountDTO {

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

    private String password;

    private String avatar;

    @JsonProperty("is_locked")
    private boolean isLocked;

    @JsonProperty("is_leader")
    private boolean isLeader;

    @JsonProperty("student_identifier")
    private String studentIdentifier;

    private String school;

    @JsonProperty("role_id")
    private Role roleId;

    @JsonProperty("team_id")
    private Team teamId;
}
