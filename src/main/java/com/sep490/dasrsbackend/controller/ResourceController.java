package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.NewResource;
import com.sep490.dasrsbackend.service.ResourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Resources", description = "Resource methods required to use for round.")
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<Object> newResource(@RequestBody @Valid NewResource resource) {
        resourceService.newResource(resource);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Resource created successfully");
    }

    @PutMapping("/{resourceId}")
    public ResponseEntity<Object> updateResource(@PathVariable Long resourceId, @RequestBody @Valid NewResource resource) {
        resourceService.updateResource(resourceId, resource);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Resource updated successfully");
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<Object> getResource(@PathVariable Long resourceId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved data ", resourceService.getResource(resourceId));
    }

    @GetMapping
    public ResponseEntity<Object> getAllResource(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved all resources", resourceService.getAllResource(pageNo, pageSize, sortBy, sortDirection));
    }

    @PutMapping("/change-status/{resourceId}")
    public ResponseEntity<Object> changeResourceStatus(@PathVariable Long resourceId, @RequestParam boolean enable) {
        resourceService.changeResourceStatus(resourceId, enable);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Resource disabled successfully");
    }


}
