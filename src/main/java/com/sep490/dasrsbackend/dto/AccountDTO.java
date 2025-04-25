package com.sep490.dasrsbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.entity.Role;
import com.sep490.dasrsbackend.model.entity.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(description = "Data Transfer Object for account information")
public class AccountDTO {

    @JsonProperty("account_id")
    @Schema(description = "Account's unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID accountId;

    @JsonProperty("first_name")
    @Schema(description = "Account's first name", example = "Nguyen Thanh")
    @Pattern(regexp = "^[^0-9]*$", message = "First name must not contain numbers")
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

    @JsonProperty("last_name")
    @Schema(description = "Account's last name", example = "Cong")
    @Pattern(regexp = "^[^0-9]*$", message = "Last name must not contain numbers")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

    @Schema(description = "Account's email address", example = "nguyen.cong@example.com")
    @Email(message = "Please enter a valid email address")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Schema(description = "Account's physical address", example = "123 Main St, Springfield")
    @NotBlank(message = "Address cannot be blank")
    @Size(max = 100, message = "Address must be less than 100 characters")
    private String address;

    @Schema(description = "Account's gender", example = "Male")
    private String gender;

    @Schema(description = "Account's date of birth", example = "2003-03-25")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @Schema(description = "Account's phone number", example = "(+84)877643231")
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "(84|0[3|5|7|8|9])([0-9]{8})\\b", message = "Please enter a valid(+84) phone number")
    private String phone;

    @Schema(description = "Account's password (hashed)", example = "********")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Schema(description = "Account's avatar URL", example = "https://example.com/avatar.png")
    private String avatar;

    @JsonProperty("is_locked")
    @Schema(description = "Indicates if the account is locked", example = "false")
    private boolean isLocked;

    @JsonProperty("is_leader")
    @Schema(description = "Indicates if the account is a team leader", example = "true")
    private boolean isLeader;

    @JsonProperty("student_identifier")
    @Schema(description = "Account's student identifier", example = "12345678")
    @NotBlank(message = "Student identifier cannot be blank")
    @Size(max = 50, message = "Student identifier must be less than 50 characters")
    private String studentIdentifier;

    @Schema(description = "Account's school name", example = "Springfield High School")
    @NotBlank(message = "School cannot be blank")
    @Size(max = 50, message = "Student identifier must be less than 50 characters")
    private String school;

    @JsonProperty("role_id")
    @Schema(description = "Account's role information")
    private Role roleId;

    @JsonProperty("team_id")
    @Schema(description = "Account's associated team")
    private Team teamId;
}
