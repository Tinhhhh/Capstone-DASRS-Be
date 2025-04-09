package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.enums.RecordStatus;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.entity.Record;
import com.sep490.dasrsbackend.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
@Tag(name = "Record", description = "APIs for managing records")
public class RecordController {

    private final RecordService recordService;

    @Operation(summary = "Submit a record with video", description = "Submit a record video against another team for review.")
    @PostMapping("/upload")
    public ResponseEntity<Object> submitRecord(
            @RequestParam Long matchId,
            @RequestParam("videoFile") MultipartFile videoFile) throws IOException {

        Record record = recordService.submitRecord(matchId, videoFile);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Record submitted successfully.");
        response.put("record", record);
        response.put("videoSize", videoFile.getSize());
        response.put("videoType", videoFile.getContentType());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
