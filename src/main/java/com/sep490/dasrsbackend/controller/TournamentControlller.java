package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.NewTournament;
import com.sep490.dasrsbackend.service.TournamentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tournament")
@RequiredArgsConstructor
@Tag(name = "Tournament", description = "method for tournament management.")
public class TournamentControlller {

    private final TournamentService tournamentService;

    @PostMapping("/new")
    public ResponseEntity<Object> newTournament(@RequestBody @Valid NewTournament request) {
        tournamentService.createTournament(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "Tournament created successfully");
    }

    @GetMapping("/get/all")
    public ResponseEntity<Object> getAllTournaments(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "All tournaments retrieved successfully",
                tournamentService.getAllTournaments(pageNo, pageSize, sortBy, sortDirection));
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getTournament(@RequestParam Long id) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Tournament retrieved successfully", tournamentService.getTournament(id));
    }

    @PutMapping("/active")
    public ResponseEntity<Object> startATournament(@RequestParam Long id) {
        tournamentService.startTournament(id);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Tournament started successfully");
    }

    @PutMapping("/change-status")
    public ResponseEntity<Object> changeTournamentStatus(@RequestParam Long id, @RequestParam TournamentStatus status) {
        tournamentService.changeStatus(id, status);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Tournament status updated successfully");
    }


}
