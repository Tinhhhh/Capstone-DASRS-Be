package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.exception.ExceptionResponse;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.AccountProfile;
import com.sep490.dasrsbackend.model.payload.request.ChangePasswordRequest;
import com.sep490.dasrsbackend.model.payload.request.NewAccountByAdmin;
import com.sep490.dasrsbackend.model.payload.request.NewAccountByStaff;
import com.sep490.dasrsbackend.model.payload.response.AccountInfoResponse;
import com.sep490.dasrsbackend.model.payload.response.UpdateAccountResponse;
import com.sep490.dasrsbackend.service.AccountService;
import com.sep490.dasrsbackend.service.implement.ExcelImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account", description = "Method for account settings required access token to gain access")
public class AccountController {

    private final AccountService accountService;
    private final ExcelImportService excelImportService;

    @Operation(summary = "Register a new account by import excel file", description = "Perform to register a new account, all the information must be filled out and cannot be blank, once requested an email will be send")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountDTO>> importAccounts(@RequestParam("file") MultipartFile file) {
        try {
            List<AccountDTO> accounts = excelImportService.importAccountsFromExcel(file.getInputStream());
            return ResponseEntity.ok(accounts);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Register a new account by admin", description = "Perform to register a new account, all the information must be filled out and cannot be blank, once requested an email will be send")
    @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "Successfully Registered", content = @Content(examples = @ExampleObject(value = """

        """))), @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ExceptionResponse.class), examples = @ExampleObject(value = """
        """)))})
    @PostMapping("/by-admin")
    public ResponseEntity<Object> AccountRegistrationByAdmin(@RequestBody @Valid NewAccountByAdmin request) throws MessagingException {
        accountService.newAccountByAdmin(request);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.ACCEPTED, "Successfully Register", "An email had been sent to email owner.");
    }

    @Operation(summary = "Change account password", description = "Allows users to change their account password.")
    @PostMapping("/change-password")
    public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        accountService.changePassword(changePasswordRequest, null);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Password changed successfully.");
    }

    @Operation(summary = "Update account profile picture", description = "Update the profile picture of an account.")
    @PutMapping("/update-profile-picture")
    public ResponseEntity<Object> updateAccountProfilePicture(@RequestParam UUID id, @RequestParam String imageURL) {
        accountService.updateAccountProfilePicture(id, imageURL);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Profile picture updated successfully.");
    }

    @Operation(summary = "Update account information", description = "Update information for the account.")
    @PutMapping("/update-info")
    public ResponseEntity<Object> updateAccountInfo(@RequestParam UUID id, @RequestBody @Valid AccountProfile accountProfile) {
        accountService.updateAccountInfo(id, accountProfile);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Account information updated successfully.");
    }

    @Operation(summary = "Get current account information", description = "Fetch the current logged-in account details.")
    @GetMapping("/current-account")
    public ResponseEntity<Object> getCurrentAccountInfo(HttpServletRequest request) {
        AccountInfoResponse accountInfo = accountService.getCurrentAccountInfo(request);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Current account information retrieved successfully.", accountInfo);
    }

    @Operation(summary = "Get account information by admin", description = "Fetch account details as an admin.")
    @GetMapping
    public ResponseEntity<Object> getAccountByAdmin(@RequestParam UUID id) {
        Object accountInfo = accountService.getAccountByAdmin(id);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Account information retrieved successfully.", accountInfo);
    }

    @Operation(summary = "Edit account by admin", description = "Allows admin to edit account details.")
    @PutMapping("/edit")
    public ResponseEntity<Object> editAccountByAdmin(@RequestParam UUID id, @RequestBody @Valid UpdateAccountResponse updateAccountResponse) {
        accountService.editAccountByAdmin(id, updateAccountResponse);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Account updated successfully.");
    }

    @Operation(summary = "Add a new player by staff", description = "Allows staff to add a new player and assign them to a team")
    @PostMapping("/staff-create")
    public ResponseEntity<Object> addPlayerByStaff(@Valid @RequestBody NewAccountByStaff request) {
        try {
            accountService.newAccountByStaff(request);
            return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "Player account created successfully.");
        } catch (MessagingException e) {
            return ResponseBuilder.responseBuilder(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email notification.");
        } catch (Exception e) {
            return ResponseBuilder.responseBuilder(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
