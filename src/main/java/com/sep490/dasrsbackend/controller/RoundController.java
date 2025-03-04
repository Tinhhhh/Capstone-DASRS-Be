package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.service.RoundService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/round")
@RequiredArgsConstructor
@Tag(name = "Round", description = "Round required to use to create tournament.")
public class RoundController {

    private final RoundService roundService;

    @PostMapping("/new")
    public ResponseEntity<Object> newRound(@RequestBody @Valid NewRound request) {
        roundService.newRound(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "New round created successfully");
    }

//    @GetMapping("/get")
//    public ResponseEntity<Object> getEnvironment(@RequestParam Long environmentId) {
//        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved data ", environmentService.getEnvironment(environmentId));
//    }
//
//    @GetMapping("/get/all")
//    public ResponseEntity<Object> getAllEnvironment(
//            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
//            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
//            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
//            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection
//    ) {
//        return ResponseBuilder.responseBuilderWithData(
//                HttpStatus.OK, "Successfully retrieved scored methods",
//                environmentService.getAllEnvironment(pageNo, pageSize, sortBy, sortDirection));
//    }

}
