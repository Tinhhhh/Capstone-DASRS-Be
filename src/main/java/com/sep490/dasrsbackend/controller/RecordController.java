package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.enums.RecordStatus;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.entity.Record;
import com.sep490.dasrsbackend.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @Operation(summary = "Submit a record", description = "Submit a record against another team for review.")
    @PostMapping
    public ResponseEntity<Object> submitRecord(@RequestParam Long matchId) {
        Record record = recordService.submitRecord(matchId);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.CREATED, "Record submitted successfully.", record);
    }
    @GetMapping
    public ResponseEntity<List<Record>> getAllRecords() {
        List<Record> records = recordService.getAllRecords();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/by-id")
    public ResponseEntity<Record> getRecordById(@RequestParam Long recordId) {
        Record record = recordService.getRecordById(recordId);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/by-match")
    public ResponseEntity<List<Record>> getRecordsByMatchId(@RequestParam Long matchId) {
        List<Record> records = recordService.getRecordsByMatchId(matchId);
        return ResponseEntity.ok(records);
    }
    @PatchMapping("/update-status")
    public ResponseEntity<Record> updateRecordStatus(@RequestParam Long recordId, @RequestParam RecordStatus status) {
        Record updatedRecord = recordService.updateRecordStatus(recordId, status);
        return ResponseEntity.ok(updatedRecord);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteRecord(@RequestParam Long recordId) {
        recordService.deleteRecord(recordId);
        return ResponseEntity.ok("Record deleted successfully");
    }
}
