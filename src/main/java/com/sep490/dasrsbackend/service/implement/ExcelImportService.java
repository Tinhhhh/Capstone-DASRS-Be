package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.converter.AccountConverter;
import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.entity.Role;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.enums.TeamStatus;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
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
                    AccountDTO accountDTO = parseRowToAccountDTO(row, teamLeaderMap);
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


    private AccountDTO parseRowToAccountDTO(Row row, Map<Long, Boolean> teamLeaderMap) {
        AccountDTO accountDTO = new AccountDTO();

        try {
            // Validate and set fields
            String email = getCellValueAsString(row.getCell(0));
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be null or empty.");
            }
            accountDTO.setEmail(validateEmail(email));
            accountDTO.setFirstName(validateNonEmpty(getCellValueAsString(row.getCell(1)), "First Name"));
            accountDTO.setLastName(validateNonEmpty(getCellValueAsString(row.getCell(2)), "Last Name"));
            accountDTO.setAddress(validateNonEmpty(getCellValueAsString(row.getCell(3)), "Address"));
            accountDTO.setGender(validateGender(getCellValueAsString(row.getCell(4))));
            accountDTO.setDob(validateDate(row.getCell(5)));
            accountDTO.setPhone(validatePhone(getCellValueAsString(row.getCell(6))));

            // Validate student identifier and school (New Fields)
            accountDTO.setStudentIdentifier(validateNonEmpty(getCellValueAsString(row.getCell(7)), "Student Identifier"));
            accountDTO.setSchool(validateNonEmpty(getCellValueAsString(row.getCell(8)), "School"));

            // Automatically assign the tournament with ACTIVE status
            Tournament tournament = tournamentRepository.findByStatus(TournamentStatus.ACTIVE)
                    .orElseThrow(() -> new IllegalArgumentException("No active tournament found. Please activate a tournament."));

            String teamName = getCellValueAsString(row.getCell(10));
            String teamTag = getCellValueAsString(row.getCell(11));
            Team team = validateOrCreateTeam(teamName, teamTag, tournament);

            if (team == null || team.getId() == null) {
                throw new IllegalArgumentException("Team could not be created or found.");
            }

            accountDTO.setTeamId(team);

            boolean isLeader = getCellValueAsBoolean(row.getCell(9));
            validateTeamLeader(team, isLeader, teamLeaderMap);
            accountDTO.setLeader(isLeader);

            String plainPassword = generateRandomPassword(8);
            accountDTO.setPassword(plainPassword);

            Role defaultRole = new Role();
            defaultRole.setId(1L);
            defaultRole.setRoleName("PLAYER");
            accountDTO.setRoleId(defaultRole);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Row error: " + e.getMessage());
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
        Team team = teamRepository.findByTeamNameAndTeamTag(teamName, teamTag).orElse(null);

        if (team == null) {
            team = new Team();
            team.setTeamName(teamName);
            team.setTeamTag(teamTag);
            team.setStatus(TeamStatus.ACTIVE);
            team.setDisqualified(false);
            team.setTournament(tournament);

            team = teamRepository.save(team);

            // Verify the team is saved properly
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
}
