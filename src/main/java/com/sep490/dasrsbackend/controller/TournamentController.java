package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.enums.TournamentSort;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.EditTournament;
import com.sep490.dasrsbackend.model.payload.request.NewTournament;
import com.sep490.dasrsbackend.service.TournamentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tournaments")
@RequiredArgsConstructor
@Tag(name = "Tournament", description = "method for tournament management.")
public class TournamentController {

    private final TournamentService tournamentService;

    @PostMapping
    public ResponseEntity<Object> newTournament(@RequestBody @Valid NewTournament request) {
        tournamentService.createTournament(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "Tournament created successfully");
    }

    @GetMapping
    public ResponseEntity<Object> getAllTournaments(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy") TournamentSort sortBy,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "All tournaments retrieved successfully",
                tournamentService.getAllTournaments(pageNo, pageSize, sortBy, keyword));
    }

    @GetMapping("/{tournamentId}")
    public ResponseEntity<Object> getTournament(@PathVariable Long tournamentId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Tournament retrieved successfully", tournamentService.getTournament(tournamentId));
    }

    @PutMapping("/active/{tournamentId}")
    public ResponseEntity<Object> startATournament(@PathVariable Long tournamentId) {
        tournamentService.startTournament(tournamentId);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Tournament started successfully");
    }

    @PutMapping("/status/{tournamentId}")
    public ResponseEntity<Object> changeTournamentStatus(@PathVariable Long tournamentId, @RequestParam TournamentStatus status) {
        tournamentService.changeStatus(tournamentId, status);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Tournament status updated successfully");
    }

    @PutMapping("/context/{tournamentId}")
    public ResponseEntity<Object> updateTournamentContext(@PathVariable Long tournamentId, @RequestBody @Valid EditTournament request) {
        tournamentService.editTournament(tournamentId, request);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Tournament updated successfully");
    }

    @PutMapping("/extend/{tournamentId}")
    public ResponseEntity<Object> updateTournamentSchedule(@PathVariable Long tournamentId, @RequestParam("day") int day) {
        tournamentService.editTournamentSchedule(tournamentId, day);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Tournament schedule updated successfully");
    }

}
