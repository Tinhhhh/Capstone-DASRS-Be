package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.entity.Record;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import com.sep490.dasrsbackend.model.enums.RecordStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.repository.MatchRepository;
import com.sep490.dasrsbackend.repository.RecordRepository;
import com.sep490.dasrsbackend.service.RecordService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final MatchRepository matchRepository;
    private final RecordRepository recordRepository;

    @Override
    public Record submitRecord(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Match not found"));

        // Check if the match status is FINISHED
        if (!MatchStatus.FINISHED.equals(match.getStatus())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Match is not completed yet");
        }

        // Build the Record instance using Lombok's builder
        Record record = Record.builder()
                .match(match)
                .status(RecordStatus.SUCCESS) // Default status
                .recordLink(generateTemporaryRecordLink()) // Placeholder for link generation
                .build();

        // Save the record
        return recordRepository.save(record);
    }

    private String generateTemporaryRecordLink() {
        // Placeholder for actual link generation logic
        return UUID.randomUUID().toString();
    }
    @Override
    public List<Record> getAllRecords() {
        List<Record> records = recordRepository.findAll();
        if (records.isEmpty()) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "No records found");
        }
        return records;
    }

    @Override
    public Record getRecordById(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Record not found"));
    }

    @Override
    public List<Record> getRecordsByMatchId(Long matchId) {
        List<Record> records = recordRepository.findByMatchId(matchId);
        if (records.isEmpty()) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "No records found for the given match ID");
        }
        return records;
    }
    @Override
    public Record updateRecordStatus(Long recordId, RecordStatus status) {
        Record record = getRecordById(recordId);

        // Update the status
        record.setStatus(status);
        return recordRepository.save(record);
    }

    @Override
    public void deleteRecord(Long recordId) {
        Record record = getRecordById(recordId);
        recordRepository.delete(record);
    }
}

