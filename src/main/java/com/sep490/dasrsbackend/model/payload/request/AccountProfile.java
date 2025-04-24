package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for account information update")
public class AccountProfile {

    @JsonIgnore
    @JsonProperty("account_id")
    private UUID id;

    @Schema(description = "Account's first name", example = "Nguyen Thanh")
    @JsonProperty("first_name")
    @Pattern(regexp = "^[^0-9]*$", message = "first name must not contain numbers")
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

    @Schema(description = "Account's last name", example = "Cong")
    @JsonProperty("last_name")
    @Pattern(regexp = "^[^0-9]*$", message = "last name must not contain numbers")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

    @Schema(description = "Account's address", example = "123 Main St, Springfield")
    @NotBlank(message = "Address cannot be blank")
    @Size(max = 100, message = "Address must be less than 100 characters")
    private String address;

    @Schema(description = "Account's gender", example = "Male")
    private String gender;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Account's date of birth", example = "2003-03-25")
    private LocalDate dob;

    @Schema(description = "User's phone number", example = "(+84)877643231")
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "(84|0[3|5|7|8|9])([0-9]{8})\\b", message = "Please enter a valid(+84) phone number")
    private String phone;


}
