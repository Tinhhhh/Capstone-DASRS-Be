package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.converter.AccountConverter;
import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.entity.Role;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.repository.AccountRepository;
import com.sep490.dasrsbackend.repository.TeamRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

@Service
public class ExcelImportService {
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountConverter accountConverter;

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
                    Account account = accountConverter.convertToEntity(accountDTO);

                    // Save account to database
                    account = accountRepository.save(account);

                    // Convert back to DTO for the response
                    accountDTOs.add(accountConverter.convertToDTO(account));
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid row: " + e.getMessage());
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

        // Validate team
        String teamName = getCellValueAsString(row.getCell(8)); // Team Name
        String teamTag = getCellValueAsString(row.getCell(9)); // Team Tag
        Team team = validateOrCreateTeam(teamName, teamTag);
        accountDTO.setTeamId(team);

        // Validate leader
        boolean isLeader = getCellValueAsBoolean(row.getCell(7)); // Is Leader
        validateTeamLeader(team, isLeader, teamLeaderMap);
        accountDTO.setLeader(isLeader);

        // Generate random password
        accountDTO.setPassword(generateRandomPassword(8));
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

    private Team validateOrCreateTeam(String teamName, String teamTag) {
        return teamRepository.findByTeamNameAndTeamTag(teamName, teamTag)
                .orElseGet(() -> {
                    Team team = new Team();
                    team.setTeamName(validateNonEmpty(teamName, "Team Name"));
                    team.setTeamTag(validateNonEmpty(teamTag, "Team Tag"));
                    return teamRepository.save(team);
                });
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
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        return email;
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

    private String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }

        return password.toString();
    }
}
