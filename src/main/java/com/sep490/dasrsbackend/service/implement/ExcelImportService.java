package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.dto.AccountDTO;
import com.sep490.dasrsbackend.model.entity.Role;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.repository.RoleRepository;
import com.sep490.dasrsbackend.repository.TeamRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelImportService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TeamRepository teamRepository;

    public List<AccountDTO> importAccountsFromExcel(InputStream inputStream) throws IOException {
        List<AccountDTO> accountDTOs = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        // Skip the header row
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            AccountDTO accountDTO = new AccountDTO();

            accountDTO.setEmail(getCellValueAsString(row.getCell(0))); // Email
            accountDTO.setFirstName(getCellValueAsString(row.getCell(1))); // First Name
            accountDTO.setLastName(getCellValueAsString(row.getCell(2))); // Last Name
            accountDTO.setAddress(getCellValueAsString(row.getCell(3))); // Address
            accountDTO.setGender(getCellValueAsString(row.getCell(4))); // Gender
            accountDTO.setDob(row.getCell(5).getLocalDateTimeCellValue().toLocalDate()); // Date of Birth
            accountDTO.setPhone(getCellValueAsString(row.getCell(6))); // Phone
            accountDTO.setLeader(getCellValueAsBoolean(row.getCell(7))); // Is Leader

            // Generate random password
            String randomPassword = generateRandomPassword(8); // Example: 8-character password
            accountDTO.setPassword(randomPassword);

            accountDTOs.add(accountDTO);
        }

        workbook.close();
        return accountDTOs;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return null;
            default:
                return cell.toString();
        }
    }

    private boolean getCellValueAsBoolean(Cell cell) {
        return cell != null && cell.getBooleanCellValue();
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
