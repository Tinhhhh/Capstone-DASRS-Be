package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.entity.Record;
import com.sep490.dasrsbackend.model.enums.RecordStatus;

import java.util.List;

public interface RecordService {
    Record submitRecord(Long matchId);
    Record getRecordById(Long recordId);
    List<Record> getRecordsByMatchId(Long matchId);
    List<Record> getAllRecords();
    Record updateRecordStatus(Long recordId, RecordStatus status);
    void deleteRecord(Long recordId);
}
