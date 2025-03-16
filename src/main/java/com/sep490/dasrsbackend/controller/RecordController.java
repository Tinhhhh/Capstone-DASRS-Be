package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.entity.Record;
import com.sep490.dasrsbackend.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
