package com.sep490.dasrsbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class ParticipantDTO {

    @JsonProperty("account_id")
    private UUID accountId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;

    private String avatar;

    private String phone;

    private String gender;

    private String dob;
}
