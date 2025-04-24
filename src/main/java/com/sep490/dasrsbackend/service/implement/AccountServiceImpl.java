package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.AccountSpecification;
import com.sep490.dasrsbackend.Util.GenerateCode;
import com.sep490.dasrsbackend.converter.AccountConverter;
import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.entity.Role;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.enums.EmailTemplateName;
import com.sep490.dasrsbackend.model.enums.PlayerSort;
import com.sep490.dasrsbackend.model.enums.RoleEnum;
import com.sep490.dasrsbackend.model.enums.RoleFilter;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.RegisterAccountExistedException;
import com.sep490.dasrsbackend.model.payload.request.AccountProfile;
import com.sep490.dasrsbackend.model.payload.request.NewAccountByAdmin;
import com.sep490.dasrsbackend.model.payload.request.NewAccountByStaff;
import com.sep490.dasrsbackend.model.payload.response.*;
import com.sep490.dasrsbackend.repository.AccountRepository;
import com.sep490.dasrsbackend.repository.RoleRepository;
import com.sep490.dasrsbackend.repository.TeamRepository;
import com.sep490.dasrsbackend.service.AccountService;
import com.sep490.dasrsbackend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
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

    public List<AccountDTO> importAccounts(InputStream excelInputStream, List<String> errorMessages) throws Exception {
        // Import Excel data
        List<AccountDTO> accountDTOs = excelImportService.importAccountsFromExcel(excelInputStream, errorMessages);
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
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Account need to be assigned to a team");
            } else {
                List<Account> accounts = accountRepository.findByTeamIdAndIsLocked(request.getTeamId(), false);
                if (accounts.size() >= 5) {
                    throw new DasrsException(HttpStatus.BAD_REQUEST, "Team is full");
                }
            }
        }

        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Team not found"));
        }

        if (request.getRoleId() >= 3) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Registration fails, invalid role");
        }

        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegisterAccountExistedException("Account already exists");
        }

        String password = GenerateCode.generateRandomPassword(9);

        Account account = Account.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .address(request.getAddress())
                .phone(request.getPhone())
                .gender("")
                .dob(null)
                .password(passwordEncoder.encode(password))
                .isLocked(false)
                .isLeader(false)
                .role(role)
                .team(null)
                .build();

        if (team != null && request.getRoleId() == 1) {
            account.setTeam(team);
        }

        accountRepository.save(account);
        sendRegistrationEmail(account, password);
    }

    private void sendRegistrationEmail(Account account, String password) throws MessagingException {
        emailService.sendAccountInformation(
                account.fullName(), account.getEmail(), password, account.getEmail(),
                EmailTemplateName.ADMIN_CREATE_ACCOUNT.getName(), "[Dasrs service] Thông tin tài khoản của bạn, cảm ơn đã sử dụng hệ thống của chúng tôi");
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

        Role role = roleRepository.findByRoleName(RoleEnum.PLAYER.getRole())
                .orElseThrow(() -> new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration fails, role not found!"));

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Team not found!"));

        long teamMemberCount = accountRepository.countByTeamId(team.getId());
        if (teamMemberCount >= 5) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Team has reached the maximum limit of 5 members.");
        }

        boolean hasLeader = accountRepository.existsByTeamIdAndIsLeaderTrue(team.getId());
        if (!hasLeader && !request.isLeader()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Team must have at least one leader.");
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
                .isLeader(request.isLeader())
                .role(role)
                .team(team)
                .build();

        accountRepository.save(account);

        sendRegistrationEmail(account, request.getPassword());
    }

    public List<PlayerResponse> getPlayerList() {
        List<Account> accounts = accountRepository.findAllPlayers();
        return accounts.stream()
                .map(account -> new PlayerResponse(
                        account.getAccountId(),
                        account.getLastName(),
                        account.getFirstName(),
                        account.getEmail(),
                        account.getGender(),
                        account.getPhone(),
                        account.getAvatar(),
                        account.isLeader(),
                        account.getTeam() != null ? account.getTeam().getId() : null,
                        account.getTeam() != null ? account.getTeam().getTeamName() : null
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlayerResponse> getPlayerByTeamName(String teamName) {

        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Team name must not be null or empty.");
        }
        List<Account> players = accountRepository.findPlayersByTeamName(teamName);
        return players.stream()
                .map(account -> new PlayerResponse(
                        account.getAccountId(),
                        account.getLastName(),
                        account.getFirstName(),
                        account.getEmail(),
                        account.getGender(),
                        account.getPhone(),
                        account.getAvatar(),
                        account.isLeader(),
                        account.getTeam() != null ? account.getTeam().getId() : null,
                        account.getTeam() != null ? account.getTeam().getTeamName() : null
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ListPlayersResponse getPlayers(int pageNo, int pageSize, PlayerSort sortBy, String keyword) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Account> spec = Specification.where(AccountSpecification.hasKeyword(keyword));

        Page<Account> playersPage = accountRepository.findAll(spec, pageable);
        List<Account> players = playersPage.getContent();

        List<PlayerResponse> playerResponses = players.stream()
                .map(account -> new PlayerResponse(
                        account.getAccountId(),
                        account.getLastName(),
                        account.getFirstName(),
                        account.getEmail(),
                        account.getGender(),
                        account.getPhone(),
                        account.getAvatar(),
                        account.isLeader(),
                        account.getTeam() != null ? account.getTeam().getId() : null,
                        account.getTeam() != null ? account.getTeam().getTeamName() : null
                ))
                .collect(Collectors.toList());

        return ListPlayersResponse.builder()
                .players(playerResponses)
                .totalPages(playersPage.getTotalPages())
                .totalElements(playersPage.getTotalElements())
                .pageNo(playersPage.getNumber())
                .pageSize(playersPage.getSize())
                .last(playersPage.isLast())
                .build();
    }

    @Override
    public ListAccountInfoResponse getAllAccount(int pageNo, int pageSize, PlayerSort sortBy, String keyword, RoleFilter role) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Specification<Account> spec = Specification.where((AccountSpecification.hasName(keyword)
                    .or(AccountSpecification.hasEmail(keyword))).and(AccountSpecification.hasRoleName(role)));

        Page<Account> accountsPage = accountRepository.findAll(spec, pageable);
        List<Account> accounts = accountsPage.getContent();
        List<AccountInfoResponse> accountInfoResponses = accounts.stream()
                .map(accountConverter::convertToAccountInfoResponse)
                .toList();

        ListAccountInfoResponse response = new ListAccountInfoResponse();
        response.setContent(accountInfoResponses);
        response.setTotalPages(accountsPage.getTotalPages());
        response.setTotalElements(accountsPage.getTotalElements());
        response.setPageNo(accountsPage.getNumber());
        response.setPageSize(accountsPage.getSize());
        response.setLast(accountsPage.isLast());
        return response;

    }
}
