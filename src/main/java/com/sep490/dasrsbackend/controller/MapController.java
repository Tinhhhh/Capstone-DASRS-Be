package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.NewMap;
import com.sep490.dasrsbackend.service.RaceMapService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/maps")
@RequiredArgsConstructor
@Tag(name = "Map", description = "Map methods required to use for round.")
public class MapController {

    private final RaceMapService mapService;

    @PostMapping
    public ResponseEntity<Object> newMap(@RequestBody @Valid NewMap newMap) {
        mapService.newMap(newMap);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Map created successfully");
    }

    @PutMapping("/{mapId}")
    public ResponseEntity<Object> updateMap(@PathVariable Long mapId, @RequestBody @Valid NewMap newMap) {
        mapService.updateMap(mapId, newMap);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Map updated successfully");
    }

    @GetMapping("/{mapId}")
    public ResponseEntity<Object> getMap(@PathVariable Long mapId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved data ", mapService.getMap(mapId));
    }

    @GetMapping
    public ResponseEntity<Object> getAllMap(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved all maps", mapService.getAllMap(pageNo, pageSize, sortBy, sortDirection));
    }


}
