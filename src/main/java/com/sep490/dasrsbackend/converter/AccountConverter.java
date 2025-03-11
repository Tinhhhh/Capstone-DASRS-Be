package com.sep490.dasrsbackend.converter;

import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.payload.request.AccountProfile;
import com.sep490.dasrsbackend.model.payload.response.AccountInfoResponse;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class AccountConverter {

    private final ModelMapper modelMapper;

    public AccountConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    // Convert Account to AccountDTO
    public AccountDTO convertToDTO(Account account) {
        if (account == null) {
            return null;
        }
        return modelMapper.map(account, AccountDTO.class);
    }

    // Convert AccountDTO to Account
    public Account convertToEntity(AccountDTO accountDTO) {
        if (accountDTO == null) {
            return null;
        }
        return modelMapper.map(accountDTO, Account.class);
    }

    // Convert Account to AccountInfoResponse
    public AccountInfoResponse convertToAccountInfoResponse(Account account) {
        if (account == null) {
            return null;
        }
        return modelMapper.map(account, AccountInfoResponse.class);
    }

    // Convert AccountProfile to Account entity (for updating)
    public void updateAccountFromProfile(Account account, AccountProfile accountProfile) {
        if (account != null && accountProfile != null) {
            account.setFirstName(accountProfile.getFirstName());
            account.setLastName(accountProfile.getLastName());
            account.setPhone(accountProfile.getPhone());
            account.setAddress(accountProfile.getAddress());
            account.setDob(accountProfile.getDob());
            account.setGender(accountProfile.getGender());
        }
    }
}
