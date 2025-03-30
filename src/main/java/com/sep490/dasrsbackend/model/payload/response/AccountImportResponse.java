package com.sep490.dasrsbackend.model.payload.response;

import com.sep490.dasrsbackend.dto.AccountImportResponseDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class AccountImportResponse {
    private List<AccountImportResponseDTO> importedAccounts;
    private List<String> errorMessages; // Display team name instead of ID
}
