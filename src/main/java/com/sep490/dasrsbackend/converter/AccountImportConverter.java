package com.sep490.dasrsbackend.converter;

import com.sep490.dasrsbackend.dto.AccountImportResponseDTO;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.payload.response.AccountImportResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountImportConverter {

    // Convert Account entity to AccountImportResponseDTO
    public AccountImportResponseDTO convertToImportResponseDTO(Account account) {
        if (account == null) {
            return null;
        }
        AccountImportResponseDTO dto = new AccountImportResponseDTO();
        dto.setAccountId(account.getAccountId());
        dto.setFirstName(account.getFirstName());
        dto.setLastName(account.getLastName());
        dto.setEmail(account.getEmail());
        dto.setAddress(account.getAddress());
        dto.setGender(account.getGender());
        dto.setDob(account.getDob());
        dto.setPhone(account.getPhone());
        dto.setAvatar(account.getAvatar());
        dto.setLocked(account.isLocked());
        dto.setLeader(account.isLeader());
        dto.setRoleName(account.getRole() != null ? account.getRole().getRoleName() : null); // Role name
        dto.setTeamName(account.getTeam() != null ? account.getTeam().getTeamName() : null); // Team name
        return dto;
    }

    // Convert a list of Account entities to a list of AccountImportResponseDTO
    public List<AccountImportResponseDTO> convertToImportResponseDTOList(List<Account> accounts) {
        return accounts.stream()
                .map(this::convertToImportResponseDTO)
                .collect(Collectors.toList());
    }

    // Create AccountImportResponse from imported accounts and error messages
    public AccountImportResponse createImportResponse(List<Account> accounts, List<String> errorMessages) {
        AccountImportResponse response = new AccountImportResponse();
        response.setImportedAccounts(convertToImportResponseDTOList(accounts));
        response.setErrorMessages(errorMessages);
        return response;
    }
}

