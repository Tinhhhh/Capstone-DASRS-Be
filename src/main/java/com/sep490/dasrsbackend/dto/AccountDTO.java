package com.sep490.dasrsbackend.dto;

import com.sep490.dasrsbackend.model.entity.Role;
import com.sep490.dasrsbackend.model.entity.Team;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AccountDTO {
    private UUID accountId;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String gender;
    private LocalDate dob;
    private String phone;
    private String password;
    private String avatar;
    private boolean isLocked;
    private boolean isLeader;
    private Role roleId;
    private Team teamId;
}