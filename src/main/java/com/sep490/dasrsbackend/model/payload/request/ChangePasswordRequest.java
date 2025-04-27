package com.sep490.dasrsbackend.model.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for changing password")
public class ChangePasswordRequest {

    @Schema(description = "User's old password", example = "Password1")
    @NotBlank(message = "Password cannot be blank")
    private String oldPassword;

    @Schema(description = "User's new password", example = "Password1")
    @NotBlank(message = "Password cannot be blank")
    private String newPassword;

}
