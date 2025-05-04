package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.converter.AccountConverter;
import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.RoleEnum;
import com.sep490.dasrsbackend.model.enums.TeamStatus;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final TeamRepository teamRepository;
    private final AccountRepository accountRepository;
    private final AccountConverter accountConverter;
    private final TournamentRepository tournamentRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CarRepository carRepository;
    private final AccountCarRepository accountCarRepository;

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);

    private static final int FIRST_NAME_MAX_LENGTH = 50;
    private static final String NAME_PATTERN_REGEX = "^[^0-9]*$";
    private static final String NAME_PATTERN_MESSAGE = "must not contain numbers";
    private static final int LAST_NAME_MAX_LENGTH = 50;
    private static final int ADDRESS_MAX_LENGTH = 100;
    private static final String PHONE_PATTERN_REGEX = "(84|0[3|5|7|8|9])([0-9]{8})\\b";
    private static final String PHONE_PATTERN_MESSAGE = "Please enter a valid(+84) phone number";

    public List<AccountDTO> importAccountsFromExcel(InputStream inputStream, List<String> errorMessages) throws IOException {
        List<AccountDTO> accountDTOs = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            Map<Long, Boolean> teamLeaderMap = new HashMap<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                if (isRowEmpty(row)) {
                    continue;
                }

                try {
                    AccountDTO accountDTO = parseRowToAccountDTO(row);
                    if (accountDTO == null) {
                        continue;
                    }
                    if (accountRepository.existsByEmail(accountDTO.getEmail())) {
                        errorMessages.add("Row " + (row.getRowNum() + 1) + ": Email " + accountDTO.getEmail() + " already exists.");
                        continue;
                    }

                    String plainPassword = accountDTO.getPassword();
                    String encodedPassword = passwordEncoder.encode(plainPassword);
                    accountDTO.setPassword(encodedPassword);
                    Account account = accountConverter.convertToEntity(accountDTO);

                    account = accountRepository.save(account);
                    accountDTOs.add(accountConverter.convertToDTO(account));

                    emailService.sendAccountInformation(
                            accountDTO.getFirstName(),
                            accountDTO.getEmail(),
                            plainPassword,
                            accountDTO.getEmail(),
                            "EMAIL_IMPORTED_ACCOUNT.html",
                            "Your Account Has Been Created"
                    );
                } catch (IllegalArgumentException e) {
                    errorMessages.add("Row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                } catch (MessagingException e) {
                    errorMessages.add("Row " + (row.getRowNum() + 1) + ": Failed to send email - " + e.getMessage());
                }
            }
        }

        if (!errorMessages.isEmpty()) {
            System.err.println("Import Errors:");
            errorMessages.forEach(System.err::println);
        }
        return accountDTOs;
    }

    private AccountDTO parseRowToAccountDTO(Row row) {
        List<String> rowErrors = new ArrayList<>();
        AccountDTO accountDTO = new AccountDTO();

        try {
            String email = getCellValueAsString(row.getCell(0));
            if (email == null || email.trim().isEmpty()) {
                rowErrors.add("Email cannot be null or empty.");
            } else {
                try {
                    accountDTO.setEmail(validateEmail(email, "Email"));
                } catch (IllegalArgumentException e) {
                    rowErrors.add(e.getMessage());
                }
            }

            try {
                accountDTO.setFirstName(validateName(getCellValueAsString(row.getCell(1)), "First Name", FIRST_NAME_MAX_LENGTH));
            } catch (IllegalArgumentException e) {
                rowErrors.add(e.getMessage());
            }

            try {
                accountDTO.setLastName(validateName(getCellValueAsString(row.getCell(2)), "Last Name", LAST_NAME_MAX_LENGTH));
            } catch (IllegalArgumentException e) {
                rowErrors.add(e.getMessage());
            }

            try {
                accountDTO.setAddress(validateAddress(getCellValueAsString(row.getCell(3)), "Address"));
            } catch (IllegalArgumentException e) {
                rowErrors.add(e.getMessage());
            }

            try {
                accountDTO.setGender(validateGender(getCellValueAsString(row.getCell(4)), "Gender"));
            } catch (IllegalArgumentException e) {
                rowErrors.add(e.getMessage());
            }

            try {
                accountDTO.setDob(validateDateOfBirth(row.getCell(5), "Date of Birth"));
            } catch (IllegalArgumentException e) {
                rowErrors.add(e.getMessage());
            }

            try {
                accountDTO.setPhone(validatePhone(getCellValueAsString(row.getCell(6)), "Phone"));
            } catch (IllegalArgumentException e) {
                rowErrors.add(e.getMessage());
            }

            try {
                accountDTO.setStudentIdentifier(validateNonEmpty(getCellValueAsString(row.getCell(7)), "Student Identifier"));
            } catch (IllegalArgumentException e) {
                rowErrors.add(e.getMessage());
            }

            try {
                accountDTO.setSchool(validateNonEmpty(getCellValueAsString(row.getCell(8)), "School"));
            } catch (IllegalArgumentException e) {
                rowErrors.add(e.getMessage());
            }

            accountDTO.setPassword(generateRandomPassword(8));

            Role defaultRole = roleRepository.findByRoleName(RoleEnum.PLAYER.name())
                    .orElseThrow(() -> new IllegalArgumentException("Default role 'PLAYER' not found in the database."));
            accountDTO.setRoleId(defaultRole);

        } catch (Exception e) {
            rowErrors.add("Unexpected error parsing row: " + e.getMessage());
        }

        if (!rowErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" | ", rowErrors));
        }

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
        Team team = teamRepository.findTeamByTeamNameAndTeamTag(teamName, teamTag).orElse(null);

        if (team == null) {
            team = new Team();
            team.setTeamName(teamName);
            team.setTeamTag(teamTag);
            team.setStatus(TeamStatus.ACTIVE);
            team.setDisqualified(false);
//            team.setTournament(tournament);

            team = teamRepository.save(team);

            if (team.getId() == null) {
                throw new IllegalArgumentException("Failed to create or retrieve team: " + teamName);
            }
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

    private String validateEmail(String email, String fieldName) {
        String trimmedEmail = validateRequiredString(email, fieldName);

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        if (!Pattern.matches(emailRegex, trimmedEmail)) {
            throw new IllegalArgumentException("Invalid email format for " + fieldName + ": " + email);
        }
        return trimmedEmail;
    }

    private String validateRequiredString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value.trim();
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

    private String validateAddress(String value, String fieldName) {
        String trimmedValue = validateRequiredString(value, fieldName);
        if (trimmedValue.length() > ADDRESS_MAX_LENGTH) {
            throw new IllegalArgumentException(fieldName + " must be less than " + ADDRESS_MAX_LENGTH + " characters.");
        }
        return trimmedValue;
    }

    private String validateNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value;
    }

    private String validateName(String value, String fieldName, int maxLength) {
        String trimmedValue = validateRequiredString(value, fieldName);
        if (trimmedValue.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be less than " + maxLength + " characters.");
        }
        if (!trimmedValue.matches(NAME_PATTERN_REGEX)) {
            throw new IllegalArgumentException(fieldName + " " + NAME_PATTERN_MESSAGE + ".");
        }
        return trimmedValue;
    }

    private String validateGender(String gender, String fieldName) {
        String trimmedGender = validateRequiredString(gender, fieldName);
        if (!trimmedGender.equalsIgnoreCase("Male") && !trimmedGender.equalsIgnoreCase("Female")) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": '" + gender + "'. Must be 'Male' or 'Female'.");
        }
        return trimmedGender.substring(0, 1).toUpperCase() + trimmedGender.substring(1).toLowerCase();
    }

    private LocalDate validateDateOfBirth(Cell cell, String fieldName) {
        if (cell == null || (cell.getCellType() != CellType.NUMERIC || !DateUtil.isCellDateFormatted(cell))) {
            if (cell != null && cell.getCellType() == CellType.STRING) {
                try {
                    LocalDate date = LocalDate.parse(cell.getStringCellValue().trim());
                    validatePastDate(date, fieldName);
                    return date;
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid date format for " + fieldName + ". Use a valid date cell format or 'YYYY-MM-DD' string.");
                }
            }
            throw new IllegalArgumentException("Invalid date format for " + fieldName + ". Use a cell formatted as Date.");
        }
        LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
        validatePastDate(date, fieldName);
        return date;
    }

    private void validatePastDate(LocalDate date, String fieldName) {
        if (!date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " must be in the past.");
        }
    }

    private String validatePhone(String phone, String fieldName) {
        String trimmedPhone = validateRequiredString(phone, fieldName);
        if (!trimmedPhone.matches(PHONE_PATTERN_REGEX)) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": '" + phone + "'. " + PHONE_PATTERN_MESSAGE + ".");
        }
        return trimmedPhone;
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
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

//    private void validateTeamSizeAndLeader(Team team, boolean isLeader, Map<Long, Boolean> teamLeaderMap) {
//        int currentMemberCount = accountRepository.countByTeamId(team.getId());
//
//        if (currentMemberCount >= 5) {
//            throw new IllegalArgumentException("Team " + team.getTeamName() + " already has 5 members. Cannot add more.");
//        }
//
//        if (isLeader) {
//            if (teamLeaderMap.getOrDefault(team.getId(), false)) {
//                throw new IllegalArgumentException("Team " + team.getTeamName() + " already has a leader.");
//            }
//            teamLeaderMap.put(team.getId(), true);
//        } else {
//            if (!teamLeaderMap.getOrDefault(team.getId(), false)) {
//                throw new IllegalArgumentException("Team " + team.getTeamName() + " must have a leader before adding members.");
//            }
//        }
//    }

}
