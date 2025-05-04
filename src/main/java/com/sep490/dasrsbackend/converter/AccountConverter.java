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

    public AccountDTO convertToDTO(Account account) {
        if (account == null) {
            return null;
        }
        return modelMapper.map(account, AccountDTO.class);
    }

    public Account convertToEntity(AccountDTO dto) {
        if (dto == null) return null;

        Account account = modelMapper.map(dto, Account.class);
        account.setRole(dto.getRoleId());
        return account;
    }

    public AccountInfoResponse convertToAccountInfoResponse(Account account) {
        return AccountInfoResponse.builder()
                .accountId(account.getAccountId())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .email(account.getEmail())
                .gender(account.getGender())
                .phone(account.getPhone())
                .avatar(account.getAvatar())
                .address(account.getAddress())
                .dob(account.getDob() != null ? account.getDob().toString() : null)
                .isLeader(account.isLeader())
                .isLocked(account.isLocked())
                .teamId(account.getTeam() != null ? account.getTeam().getId() : null)
                .teamName(account.getTeam() != null ? account.getTeam().getTeamName() : null)
                .roleId(account.getRole() != null ? account.getRole().getId() : null)
                .roleName(account.getRole() != null ? account.getRole().getRoleName() : null)
                .build();
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
