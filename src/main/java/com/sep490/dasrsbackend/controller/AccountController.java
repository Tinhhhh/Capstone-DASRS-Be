package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.service.AccountService;
import com.sep490.dasrsbackend.service.implement.ExcelImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private ExcelImportService excelImportService;

    @PostMapping("/import")
    public ResponseEntity<List<AccountDTO>> importAccounts(@RequestParam("file") MultipartFile file) {
        try {
            List<AccountDTO> accounts = excelImportService.importAccountsFromExcel(file.getInputStream());
            return ResponseEntity.ok(accounts);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
