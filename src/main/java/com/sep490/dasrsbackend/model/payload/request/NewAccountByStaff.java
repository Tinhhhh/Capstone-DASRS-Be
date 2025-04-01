package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for new Player account creation by staff")
public class NewAccountByStaff {

    @Schema(description = "User's first name", example = "Vo Van")
    @NotEmpty(message = "First name is mandatory")
    @JsonProperty("first_name")
    @Pattern(regexp = "^[^0-9]*$", message = "First name must not contain numbers")
    private String firstName;

    @Schema(description = "User's last name", example = "Tinh")
    @NotEmpty(message = "Last name is mandatory")
    @JsonProperty("last_name")
    @Pattern(regexp = "^[^0-9]*$", message = "Last name must not contain numbers")
    private String lastName;

    @Schema(description = "User's email address", example = "java@example.com")
    @NotEmpty(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "User's address", example = "123 Main St, Springfield")
    private String address;

    @Schema(description = "User's phone number", example = "(+84)794801006")
    @Pattern(regexp = "(84|0[3|5|7|8|9])+([0-9]{8})\\b", message = "Please enter a valid (+84) phone number")
    private String phone;

    @Schema(description = "User's password", example = "Password1")
    @NotEmpty(message = "Password cannot be blank")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,16}$", message = "Minimum 8 characters, at least one uppercase letter and number")
    private String password;

    @Schema(description = "Team ID to assign the player to", example = "1")
    @JsonProperty("team_id")
    private Long teamId;

    @Schema(description = "User's date of birth", example = "2000-01-01")
    @NotNull(message = "Date of birth is mandatory")
    private LocalDate dob;

    @Schema(description = "User's gender", example = "Male")
    @NotEmpty(message = "Gender is mandatory")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    private String gender;
}
