package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.NewMatchType;
import com.sep490.dasrsbackend.service.MatchTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/match-type")
@RequiredArgsConstructor
@Tag(name = "MatchType", description = "Match type required to use for round.")
public class MatchTypeController {

    private final MatchTypeService matchTypeService;

    @PostMapping("/new")
    public ResponseEntity<Object> NewMatchType(@RequestBody @Valid NewMatchType request) {
        matchTypeService.newMatchType(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "New match type created successfully");
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getMatchType(@RequestParam Long matchTypeId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved data ", matchTypeService.getMatchType(matchTypeId));
    }

    @GetMapping("/get/all")
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
}
