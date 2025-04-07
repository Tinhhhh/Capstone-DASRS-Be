package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.EditMatchType;
import com.sep490.dasrsbackend.model.payload.request.NewMatchType;
import com.sep490.dasrsbackend.service.MatchTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/match-types")
@RequiredArgsConstructor
@Tag(name = "Match type", description = "Match type required to use for round.")
public class MatchTypeController {

    private final MatchTypeService matchTypeService;

    @PostMapping
    public ResponseEntity<Object> newMatchType(@RequestBody @Valid NewMatchType request) {
        matchTypeService.newMatchType(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "New match type created successfully");
    }

    @GetMapping("/{matchTypeId}")
    public ResponseEntity<Object> getMatchType(@PathVariable Long matchTypeId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved data ", matchTypeService.getMatchType(matchTypeId));
    }

    @GetMapping
    public ResponseEntity<Object> getAllMatchType(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection
    ) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved all match type",
                matchTypeService.getAllMatchType(pageNo, pageSize, sortBy, sortDirection));
    }

    @PutMapping("/{matchTypeId}")
    @Operation(summary = "Update match type", description = "Update an existing match type's information")
    public ResponseEntity<Object> updateMatchType(
            @PathVariable Long matchTypeId,
            @RequestBody @Valid EditMatchType request
    ) {
        matchTypeService.updateMatchType(matchTypeId, request);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Match type updated successfully");
    }

    @DeleteMapping("/{matchTypeId}")
    @Operation(summary = "Delete match type", description = "Soft delete a match type by setting its status to INACTIVE")
    public ResponseEntity<Object> deleteMatchType(@PathVariable Long matchTypeId) {
        matchTypeService.deleteMatchType(matchTypeId);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Match type marked as INACTIVE");
    }
}
