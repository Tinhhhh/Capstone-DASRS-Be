package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.exception.ExceptionResponse;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.AuthenticationRequest;
import com.sep490.dasrsbackend.model.payload.request.NewAccountByAdmin;
import com.sep490.dasrsbackend.service.AuthenService;
import com.sep490.dasrsbackend.service.implement.ExcelImportService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@OpenAPIDefinition(info = @Info(
        title = "DASRS REST API", version = "1.0", description = "API documentation for Digital Automotive Simulation Racing System",
        contact = @Contact(name = "Vo Van Tinh", url = "https://github.com/Tinhhhh")),
        security = {@SecurityRequirement(name = "bearerToken")}
)
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication required to use other resources.")
public class AuthController {

    private final AuthenService authService;
    private final ExcelImportService excelImportService;

    @PostMapping("/import")
    public ResponseEntity<List<AccountDTO>> importAccounts(@RequestParam("file") MultipartFile file) {
        try {
            List<AccountDTO> accounts = excelImportService.importAccountsFromExcel(file.getInputStream());
            return ResponseEntity.ok(accounts);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Login in to the system", description = "Login into the system requires all information to be provided, " + "and validations will be performed. The response will include an access token and a refresh token")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully Login", content = @Content(examples = @ExampleObject(value = """
            {
               "http_status": 200,
               "time_stamp": "10/29/2024 11:20:03",
               "message": "Successfully SignIn",
               "data": {
                 "access_token": "xxxx.yyyy.zzzz",
                 "refresh_token": "xxxx.yyyy.zzzz"
            }
        """))),})
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> SignIn(@RequestBody @Valid AuthenticationRequest request) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully Sign in", authService.authenticate(request));
    }

    @Operation(summary = "Register a new account by admin", description = "Perform to register a new account, all the information must be filled out and cannot be blank, once requested an email will be send")
    @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "Successfully Registered", content = @Content(examples = @ExampleObject(value = """

        """))), @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ExceptionResponse.class), examples = @ExampleObject(value = """
        """)))})
    @PostMapping("/registration-admin")
    public ResponseEntity<Object> AccountRegistrationByAdmin(@RequestBody @Valid NewAccountByAdmin request) throws MessagingException {
        authService.newAccountByAdmin(request);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.ACCEPTED, "Successfully Register", "An email had been sent to email owner.");
    }

}
