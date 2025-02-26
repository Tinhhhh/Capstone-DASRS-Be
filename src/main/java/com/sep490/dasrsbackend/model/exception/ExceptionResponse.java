package com.sep490.dasrsbackend.model.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Object for Exception Response")
public class ExceptionResponse {
    @Schema(description = "Http Status Code", example = "200")
    private Integer httpStatus;
    @Schema(description = "Time that the error occur", example = "25/02/2025 15:32:00")
    private String timestamp;
    @Schema(description = "Error title", example = "Invalid Email/Password")
    private String message;
    @Schema(description = "An Error detail", example = "Invalid Email/Password")
    private String error;

    @Schema(description = "List Of Error Details", example = """
            data: {
              "last_name": "Last name is mandatory",
              "password": "Password cannot be blank",
              "phone": "Please enter a valid (+84) phone number"
            }""")
    private Map<String, String> data;

}
