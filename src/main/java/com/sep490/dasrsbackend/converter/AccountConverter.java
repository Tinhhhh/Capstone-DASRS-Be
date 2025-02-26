package com.sep490.dasrsbackend.converter;

import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.entity.Account;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class AccountConverter {

    private final ModelMapper modelMapper;

    public AccountConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public AccountDTO convertToDTO(Account account) {
        return modelMapper.map(account, AccountDTO.class);
    }

    public Account convertToEntity(AccountDTO accountDTO) {
        return modelMapper.map(accountDTO, Account.class);
    }
}