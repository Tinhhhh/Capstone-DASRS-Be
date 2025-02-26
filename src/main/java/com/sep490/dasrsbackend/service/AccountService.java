package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.dto.AccountDTO;

import java.io.InputStream;
import java.util.List;

public interface AccountService {
    List<AccountDTO> createAccounts(List<AccountDTO> accountDTOs);
    List<AccountDTO> importAccounts(InputStream excelInputStream) throws Exception;
}