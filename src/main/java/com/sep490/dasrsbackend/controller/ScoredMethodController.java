package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
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
@RequestMapping("/api/v1/scored-method")
@RequiredArgsConstructor
@Tag(name = "ScoredMethod", description = "Scored method required to use for round.")
public class ScoredMethodController {

    private final ScoredMethodService scoredMethodService;

    @PostMapping("/new")
    public ResponseEntity<Object> NewScoredMethod(@RequestBody @Valid NewScoreMethod request) {
        scoredMethodService.createNewScoredMethod(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "Scored method created successfully");
    }

    @PutMapping("/edit")
    public ResponseEntity<Object> updateScoredMethod(@RequestParam Long scoredMethodId, @RequestBody @Valid NewScoreMethod request) {
        scoredMethodService.updateScoredMethod(scoredMethodId, request);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Scored method updated successfully");
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getScoredMethod(@RequestParam Long scoredMethodId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved scored method", scoredMethodService.getScoredMethod(scoredMethodId));
    }

    @GetMapping("/get/all")
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



}
