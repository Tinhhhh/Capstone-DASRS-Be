package com.sep490.dasrsbackend.model.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user sign in")
public class AuthenticationRequest {

    @Schema(description = "User's email address", example = "john.doe@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "email must be less than 50 characters")
    private String email;

    @Schema(description = "User's password", example = "Password1")
    @NotBlank(message = "Password cannot be blank")
    @Size(max = 50, message = "password must be less than 50 characters")
    private String password;


}
