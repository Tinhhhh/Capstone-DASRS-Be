package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.NewEnvironment;
import com.sep490.dasrsbackend.service.EnvironmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/environments")
@RequiredArgsConstructor
@Tag(name = "Environment", description = "Environment required to use for round.")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    @PostMapping
    public ResponseEntity<Object> newEnvironment(@RequestBody @Valid NewEnvironment request) {
        environmentService.newEnvironment(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "New environment created successfully");
    }

    @GetMapping("/{environmentId}")
    public ResponseEntity<Object> getEnvironment(@PathVariable Long environmentId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved data ", environmentService.getEnvironment(environmentId));
    }

    @GetMapping
    public ResponseEntity<Object> getAllEnvironment(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection
    ) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved scored methods",
                environmentService.getAllEnvironment(pageNo, pageSize, sortBy, sortDirection));
    }
}
