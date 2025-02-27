package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.payload.request.AccountProfile;
import com.sep490.dasrsbackend.model.payload.request.ChangePasswordRequest;
import com.sep490.dasrsbackend.model.payload.response.AccountInfoResponse;
import com.sep490.dasrsbackend.model.payload.response.UpdateAccountResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<AccountDTO> createAccounts(List<AccountDTO> accountDTOs);

    List<AccountDTO> importAccounts(InputStream excelInputStream) throws Exception;

    AccountInfoResponse getCurrentAccountInfo(HttpServletRequest request);

    void changePassword(ChangePasswordRequest changePasswordRequest, HttpServletRequest request);

    void updateAccountProfilePicture(UUID id, String imageURLMain);

    void updateAccountInfo(UUID id, AccountProfile accountProfile);

//    List<AccountDTO> getAllAccount(int pageNo, int pageSize, String sortBy, String keyword, String role);

    AccountInfoResponse getAccountByAdmin(UUID accountId);

    void editAccountByAdmin(UUID accountId, UpdateAccountResponse updateAccountResponse);


}