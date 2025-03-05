package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.payload.request.RecordRequest;
import com.sep490.dasrsbackend.model.payload.response.RecordResponse;
import com.sep490.dasrsbackend.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    public ResponseEntity<RecordResponse> createRecord(@Valid @RequestBody RecordRequest request) {
        return ResponseEntity.ok(recordService.createRecord(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecordResponse> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(recordService.getRecordById(id));
    }

    @GetMapping
    public ResponseEntity<List<RecordResponse>> getAllRecords() {
        return ResponseEntity.ok(recordService.getAllRecords());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecordResponse> updateRecord(@PathVariable Long id, @Valid @RequestBody RecordRequest request) {
        return ResponseEntity.ok(recordService.updateRecord(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}
