package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.converter.AccountConverter;
import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.entity.Role;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.enums.TeamStatus;
import com.sep490.dasrsbackend.repository.AccountRepository;
import com.sep490.dasrsbackend.repository.TeamRepository;
import com.sep490.dasrsbackend.repository.TournamentRepository;
import com.sep490.dasrsbackend.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final TeamRepository teamRepository;
    private final AccountRepository accountRepository;
    private final AccountConverter accountConverter;
    private final TournamentRepository tournamentRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public List<AccountDTO> importAccountsFromExcel(InputStream inputStream) throws IOException {
        List<AccountDTO> accountDTOs = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip the header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            Map<Long, Boolean> teamLeaderMap = new HashMap<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    AccountDTO accountDTO = parseRowToAccountDTO(row, teamLeaderMap);
                    // Keep the plain password for the email
                    String plainPassword = accountDTO.getPassword();

                    // Encode the password for storing in the database
                    String encodedPassword = passwordEncoder.encode(plainPassword);
                    accountDTO.setPassword(encodedPassword);
                    Account account = accountConverter.convertToEntity(accountDTO);

                    // Save account to database
                    account = accountRepository.save(account);

                    // Convert back to DTO for the response
                    accountDTOs.add(accountConverter.convertToDTO(account));

                    emailService.sendAccountInformation(
                            accountDTO.getFirstName(),
                            accountDTO.getEmail(),
                            plainPassword, // Send the plain password here
                            accountDTO.getEmail(),
                            "EMAIL_IMPORTED_ACCOUNT.html", // Template name
                            "Your Account Has Been Created"
                    );
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid row: " + e.getMessage());
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return accountDTOs;
    }

    private AccountDTO parseRowToAccountDTO(Row row, Map<Long, Boolean> teamLeaderMap) {
        AccountDTO accountDTO = new AccountDTO();

        // Read and validate fields
        accountDTO.setEmail(validateEmail(getCellValueAsString(row.getCell(0)))); // Email
        accountDTO.setFirstName(validateNonEmpty(getCellValueAsString(row.getCell(1)), "First Name")); // First Name
        accountDTO.setLastName(validateNonEmpty(getCellValueAsString(row.getCell(2)), "Last Name")); // Last Name
        accountDTO.setAddress(validateNonEmpty(getCellValueAsString(row.getCell(3)), "Address")); // Address
        accountDTO.setGender(validateGender(getCellValueAsString(row.getCell(4)))); // Gender
        accountDTO.setDob(validateDate(row.getCell(5))); // Date of Birth
        accountDTO.setPhone(validatePhone(getCellValueAsString(row.getCell(6)))); // Phone

        Long tournamentId = getCellValueAsLong(row.getCell(10)); // Assuming Tournament ID is in column 10
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament ID " + tournamentId + " not found"));

        // Validate team
        String teamName = getCellValueAsString(row.getCell(8)); // Team Name
        String teamTag = getCellValueAsString(row.getCell(9)); // Team Tag
        Team team = validateOrCreateTeam(teamName, teamTag, tournament);
        accountDTO.setTeamId(team);

        // Validate leader
        boolean isLeader = getCellValueAsBoolean(row.getCell(7)); // Is Leader
        validateTeamLeader(team, isLeader, teamLeaderMap);
        accountDTO.setLeader(isLeader);

        // Generate random password
        String plainPassword = generateRandomPassword(8);
        accountDTO.setPassword(plainPassword);
        // Set default Role object
        Role defaultRole = new Role();
        defaultRole.setId(1L); // Default role ID
        defaultRole.setRoleName("PLAYER"); // Default role name
        accountDTO.setRoleId(defaultRole);

        return accountDTO;
    }

    private void validateTeamLeader(Team team, boolean isLeader, Map<Long, Boolean> teamLeaderMap) {
        if (isLeader) {
            if (teamLeaderMap.getOrDefault(team.getId(), false)) {
                throw new IllegalArgumentException("Team already has a leader: " + team.getTeamName());
            }
            teamLeaderMap.put(team.getId(), true);
        }
    }

    private Team validateOrCreateTeam(String teamName, String teamTag, Tournament tournament) {
        // Check if the team already exists
        Team team = teamRepository.findByTeamNameAndTeamTag(teamName, teamTag)
                .orElse(null);

        if (team == null) {
            // Create a new team
            team = new Team();
            team.setTeamName(teamName);
            team.setTeamTag(teamTag);
            team.setStatus(TeamStatus.ACTIVE); // Default status
            team.setDisqualified(false);
            team.setTournament(tournament);

            // Save the team
            team = teamRepository.save(team);
        }

        return team;
    }


    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return null;
            default:
                return cell.toString().trim();
        }
    }

    private boolean getCellValueAsBoolean(Cell cell) {
        return cell != null && cell.getBooleanCellValue();
    }

    private String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        // Enhanced email regex to cover a wide range of valid email addresses
        String emailRegex = "^(?=.{1,254}$)(?=.{1,64}@.{1,255}$)[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        return email;
    }


    private Long getCellValueAsLong(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            throw new IllegalArgumentException("Tournament ID cannot be blank");
        }
        try {
            return (long) cell.getNumericCellValue();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Tournament ID: " + cell.toString());
        }
    }


    private String validateNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value;
    }

    private String validateGender(String gender) {
        if (gender == null || (!gender.equalsIgnoreCase("Male") && !gender.equalsIgnoreCase("Female"))) {
            throw new IllegalArgumentException("Invalid gender: " + gender);
        }
        return gender;
    }

    private LocalDate validateDate(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC || !DateUtil.isCellDateFormatted(cell)) {
            throw new IllegalArgumentException("Invalid date format.");
        }
        return cell.getLocalDateTimeCellValue().toLocalDate();
    }

    private String validatePhone(String phone) {
        if (phone == null || !phone.matches("^\\d{10,15}$")) {
            throw new IllegalArgumentException("Invalid phone number: " + phone);
        }
        return phone;
    }

    public String generateRandomPassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            password.append(characters.charAt(index));
        }
        return password.toString();
    }
}
