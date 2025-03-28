package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.converter.AccountConverter;
import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.entity.Role;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.enums.EmailTemplateName;
import com.sep490.dasrsbackend.model.enums.RoleEnum;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.RegisterAccountExistedException;
import com.sep490.dasrsbackend.model.payload.request.AccountProfile;
import com.sep490.dasrsbackend.model.payload.request.ChangePasswordRequest;
import com.sep490.dasrsbackend.model.payload.request.NewAccountByAdmin;
import com.sep490.dasrsbackend.model.payload.request.NewAccountByStaff;
import com.sep490.dasrsbackend.model.payload.response.AccountInfoResponse;
import com.sep490.dasrsbackend.model.payload.response.UpdateAccountResponse;
import com.sep490.dasrsbackend.repository.AccountRepository;
import com.sep490.dasrsbackend.repository.RoleRepository;
import com.sep490.dasrsbackend.repository.TeamRepository;
import com.sep490.dasrsbackend.service.AccountService;
import com.sep490.dasrsbackend.service.EmailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountConverter accountConverter;
    private final ExcelImportService excelImportService;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret-key}")
    private String secretKey;

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

    @Override
    public AccountInfoResponse getCurrentAccountInfo(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Account not found"));

        return accountConverter.convertToAccountInfoResponse(account);
    }


    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        Account account = accountRepository.findByEmail(username)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Account not found"));

        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    @Override
    public void updateAccountProfilePicture(UUID id, String imageURLMain) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setAvatar(imageURLMain); // Use setAvatar instead of setProfilePicture
        accountRepository.save(account);
    }

    @Override
    public void updateAccountInfo(UUID id, AccountProfile accountProfile) {
        // Retrieve the account by ID
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Account not found"));

        // Update account information
        account.setFirstName(accountProfile.getFirstName());
        account.setLastName(accountProfile.getLastName());
        account.setPhone(accountProfile.getPhone());
        account.setAddress(accountProfile.getAddress());
        account.setDob(accountProfile.getDob());
        account.setGender(accountProfile.getGender());

        // Save the updated account
        accountRepository.save(account);
    }


    @Override
    public AccountInfoResponse getAccountByAdmin(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Account not found"));

        return accountConverter.convertToAccountInfoResponse(account);
    }

    @Override
    public void editAccountByAdmin(UUID accountId, UpdateAccountResponse updateAccountResponse) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Account not found"));

        account.setFirstName(updateAccountResponse.getFirstName());
        account.setLastName(updateAccountResponse.getLastName());
        account.setPhone(updateAccountResponse.getPhone());
        account.setAddress(updateAccountResponse.getAddress());
        account.setDob(updateAccountResponse.getDob());
        account.setGender(updateAccountResponse.getGender());
        account.setRole(roleRepository.findById(updateAccountResponse.getRoleId())
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Role not found")));

        accountRepository.save(account);
    }


    @Override
    public void newAccountByAdmin(NewAccountByAdmin request) throws MessagingException {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration fails, role not found !"));

        if (request.getRoleId() == 1) {
            if (request.getTeamId() == null) {
                throw new DasrsException(HttpStatus.BAD_REQUEST,"Account need to be assigned to a team");
            }
        }

        Team team = teamRepository.findById(request.getTeamId()).orElse(null);

        if (request.getRoleId() >= 4) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Registration fails, invalid role");
        }

        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegisterAccountExistedException("Account already exists");
        }

        Account account = Account.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .address(request.getAddress())
                .phone(request.getPhone())
                .gender("")
                .dob(null)
                .password(passwordEncoder.encode(request.getPassword()))
                .isLocked(false)
                .isLeader(false)
                .role(role)
                .team(null)
                .build();

        if (team != null && request.getRoleId() == 1) {
            account.setTeam(team);
        }

        accountRepository.save(account);
        sendRegistrationEmail(account, request.getPassword());
    }

    private void sendRegistrationEmail(Account account, String password) throws MessagingException {
        emailService.sendAccountInformation(
                account.fullName(), account.getEmail(), password, account.getEmail(),
                EmailTemplateName.ADMIN_CREATE_ACCOUNT.getName(), "[Dasrs] Thông tin tài khoản của bạn");
    }

    private UUID extractAccountIdFromRequest(HttpServletRequest request) {
        String accountIdHeader = request.getHeader("Account-Id");
        if (accountIdHeader == null || accountIdHeader.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Account ID is missing in the request");
        }

        try {
            return UUID.fromString(accountIdHeader);
        } catch (IllegalArgumentException e) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Invalid Account ID format");
        }
    }

    @Override
    public void newAccountByStaff(NewAccountByStaff request) throws MessagingException {
        // Ensure the role is set to "PLAYER"
        Role role = roleRepository.findByRoleName(RoleEnum.PLAYER.getRole())
                .orElseThrow(() -> new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration fails, role not found!"));

        // Check if the team exists
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Team not found!"));

        // Check for duplicate email
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegisterAccountExistedException("Account already exists");
        }

        // Build the new account
        Account account = Account.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .address(request.getAddress())
                .phone(request.getPhone())
                .gender("") // Default or leave null
                .dob(null)  // Default or leave null
                .password(passwordEncoder.encode(request.getPassword()))
                .isLocked(false)
                .isLeader(false)
                .role(role) // Always PLAYER role
                .team(team) // Assign to the provided team
                .build();

        accountRepository.save(account);

        // Send registration email
        sendRegistrationEmail(account, request.getPassword());
    }
}
