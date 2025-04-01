package com.sep490.dasrsbackend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AccountImportResponseDTO {
    private UUID accountId;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String gender;
    private LocalDate dob;
    private String phone;
    private String avatar;
    private boolean isLocked;
    private boolean isLeader;
    private String roleName; // Display role name
    private String teamName; // Display team name
}
