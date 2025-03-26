package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.entity.Record;
import com.sep490.dasrsbackend.model.enums.RecordStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RecordService {
    Record submitRecord(Long matchId, MultipartFile videoFile);
    Record getRecordById(Long recordId);
    List<Record> getRecordsByMatchId(Long matchId);
    List<Record> getAllRecords();
    Record updateRecordStatus(Long recordId, RecordStatus status);
    void deleteRecord(Long recordId);
}
