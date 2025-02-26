package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.converter.AccountConverter;
import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.repository.AccountRepository;
import com.sep490.dasrsbackend.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountConverter accountConverter;

    @Autowired
    private ExcelImportService excelImportService;

    @Override
    public List<AccountDTO> createAccounts(List<AccountDTO> accountDTOs) {
        List<Account> accounts = accountDTOs.stream()
                .map(accountConverter::convertToEntity)
                .collect(Collectors.toList());
        List<Account> savedAccounts = accountRepository.saveAll(accounts);
        return savedAccounts.stream()
                .map(accountConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AccountDTO> importAccounts(InputStream excelInputStream) throws Exception {
        // Import Excel data
        List<AccountDTO> accountDTOs = excelImportService.importAccountsFromExcel(excelInputStream);
        // Save accounts in the database
        return createAccounts(accountDTOs);
    }
}
