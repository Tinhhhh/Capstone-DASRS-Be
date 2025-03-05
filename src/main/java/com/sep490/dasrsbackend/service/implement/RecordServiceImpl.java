package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.converter.RecordConverter;
import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.entity.Record;
import com.sep490.dasrsbackend.model.payload.request.RecordRequest;
import com.sep490.dasrsbackend.model.payload.response.RecordResponse;
import com.sep490.dasrsbackend.repository.MatchRepository;
import com.sep490.dasrsbackend.repository.RecordRepository;
import com.sep490.dasrsbackend.service.RecordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecordServiceImpl implements RecordService {

    private final RecordRepository recordRepository;
    private final MatchRepository matchRepository;
    private final RecordConverter recordConverter;

    public RecordServiceImpl(RecordRepository recordRepository, MatchRepository matchRepository, RecordConverter recordConverter) {
        this.recordRepository = recordRepository;
        this.matchRepository = matchRepository;
        this.recordConverter = recordConverter;
    }

    @Override
    public RecordResponse createRecord(RecordRequest request) {
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found with ID: " + request.getMatchId()));

        Record record = recordConverter.toEntity(request, match);
        Record savedRecord = recordRepository.save(record);
        return recordConverter.toResponse(savedRecord);
    }

    @Override
    public RecordResponse getRecordById(Long id) {
        Record record = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found with ID: " + id));
        return recordConverter.toResponse(record);
    }

    @Override
    public List<RecordResponse> getAllRecords() {
        return recordRepository.findAll().stream()
                .map(recordConverter::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RecordResponse updateRecord(Long id, RecordRequest request) {
        Record existingRecord = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found with ID: " + id));

        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found with ID: " + request.getMatchId()));

        existingRecord.setRecordLink(request.getRecordLink());
        existingRecord.setStatus(request.getStatus());
        existingRecord.setMatch(match);

        Record updatedRecord = recordRepository.save(existingRecord);
        return recordConverter.toResponse(updatedRecord);
    }

    @Override
    public void deleteRecord(Long id) {
        if (!recordRepository.existsById(id)) {
            throw new IllegalArgumentException("Record not found with ID: " + id);
        }
        recordRepository.deleteById(id);
    }
}
