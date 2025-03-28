package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.enums.ScoredMethodStatus;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.NewScoreMethod;
import com.sep490.dasrsbackend.service.ScoredMethodService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scored-methods")
@RequiredArgsConstructor
@Tag(name = "Scored method", description = "Scored method required to use for round.")
public class ScoredMethodController {

    private final ScoredMethodService scoredMethodService;

    @PostMapping
    public ResponseEntity<Object> newScoredMethod(@RequestBody @Valid NewScoreMethod request) {
        scoredMethodService.createNewScoredMethod(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "Scored method created successfully");
    }

    @PutMapping
    public ResponseEntity<Object> updateScoredMethod(@RequestParam Long scoredMethodId, @RequestBody @Valid NewScoreMethod request) {
        scoredMethodService.updateScoredMethod(scoredMethodId, request);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Scored method updated successfully");
    }

    @GetMapping("/{scoredMethodId}")
    public ResponseEntity<Object> getScoredMethod(@PathVariable Long scoredMethodId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved scored method", scoredMethodService.getScoredMethod(scoredMethodId));
    }

    @GetMapping
    public ResponseEntity<Object> getAllScoredMethods(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection
    ) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved scored methods",
                scoredMethodService.getAllScoredMethods(pageNo, pageSize, sortBy, sortDirection));
    }

    @PutMapping("/change-status")
    public ResponseEntity<Object> changeStatus(@RequestParam Long scoredMethodId, @RequestParam ScoredMethodStatus status) {
        scoredMethodService.changeStatus(scoredMethodId, status);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Scored method status changed successfully");
    }

}
