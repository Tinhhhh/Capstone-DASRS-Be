package com.sep490.dasrsbackend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ParticipantDTO {
    private UUID accountId;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;
    private String phone;
    private String gender;
    private LocalDate dob;
}

