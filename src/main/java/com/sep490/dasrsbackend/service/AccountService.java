package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.enums.AccountSort;
import com.sep490.dasrsbackend.model.enums.PlayerSort;
import com.sep490.dasrsbackend.model.enums.RoleFilter;
import com.sep490.dasrsbackend.model.payload.request.AccountProfile;
import com.sep490.dasrsbackend.model.payload.request.NewAccountByAdmin;
import com.sep490.dasrsbackend.model.payload.request.NewAccountByStaff;
import com.sep490.dasrsbackend.model.payload.response.*;
import jakarta.mail.MessagingException;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<AccountDTO> createAccounts(List<AccountDTO> accountDTOs);

    List<AccountDTO> importAccounts(InputStream excelInputStream, List<String> errorMessages) throws Exception;

    AccountInfoResponse getCurrentAccountInfo(String email);

    void changePassword(String username, String oldPassword, String newPassword);

    void updateAccountProfilePicture(UUID id, String imageURLMain);

    void updateAccountInfo(UUID id, AccountProfile accountProfile);

//    List<AccountDTO> getAllAccount(int pageNo, int pageSize, String sortBy, String keyword, String role);

    AccountInfoResponse getAccountByAdmin(UUID accountId);

    void editAccountByAdmin(UUID accountId, UpdateAccountResponse updateAccountResponse);

    void newAccountByAdmin(NewAccountByAdmin account) throws MessagingException;

    void newAccountByStaff(NewAccountByStaff request) throws MessagingException;

    List<PlayerResponse> getPlayerByTeamName(String teamName);

    ListPlayersResponse getPlayers(int pageNo, int pageSize, PlayerSort sortBy, String keyword);

    //For admin
    ListAccountInfoResponse getAllAccount(int pageNo, int pageSize, AccountSort sortBy, String keyword, RoleFilter role);

    void lockAccountByAdmin(UUID accountId);


}