package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.request.RecordRequest;
import com.sep490.dasrsbackend.model.payload.response.RecordResponse;

import java.util.List;

public interface RecordService {

    RecordResponse createRecord(RecordRequest request);

    RecordResponse getRecordById(Long id);

    List<RecordResponse> getAllRecords();

    RecordResponse updateRecord(Long id, RecordRequest request);

    void deleteRecord(Long id);
}
