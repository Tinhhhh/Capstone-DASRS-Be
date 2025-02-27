package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.exception.ExceptionResponse;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.newAccountByAdminRequest;
import com.sep490.dasrsbackend.service.AccountService;
import com.sep490.dasrsbackend.service.AuthenService;
import com.sep490.dasrsbackend.service.implement.ExcelImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private ExcelImportService excelImportService;
    private final AuthenService authService;

    @PostMapping("/import")
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
    @PostMapping("/registration-admin")
    public ResponseEntity<Object> AccountRegistrationByAdmin(@RequestBody @Valid newAccountByAdminRequest request) throws MessagingException {
        authService.newAccountByAdmin(request);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.ACCEPTED, "Successfully Register", "An email had been sent to email owner.");
    }

}
