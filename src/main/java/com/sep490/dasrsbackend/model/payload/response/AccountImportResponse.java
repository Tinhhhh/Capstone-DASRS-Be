package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.dto.AccountImportResponseDTO;
import lombok.Data;

import java.util.List;

@Data
public class AccountImportResponse {

    @JsonProperty("imported_accounts")
    private List<AccountImportResponseDTO> importedAccounts;

    @JsonProperty("error_messages")
    private List<String> errorMessages; // Display team name instead of ID
}
