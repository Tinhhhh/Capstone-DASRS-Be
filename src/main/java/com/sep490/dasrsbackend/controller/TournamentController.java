package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.enums.TournamentSort;
import com.sep490.dasrsbackend.model.enums.TournamentStatusFilter;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.EditTournament;
import com.sep490.dasrsbackend.model.payload.request.NewTournament;
import com.sep490.dasrsbackend.model.payload.response.TournamentByTeamResponse;
import com.sep490.dasrsbackend.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tournaments")
@RequiredArgsConstructor
@Tag(name = "Tournament", description = "method for tournament management.")
public class TournamentController {

    private final TournamentService tournamentService;

    @Operation(summary = "Create a new tournament", description = "Create a new tournament with the provided details.")
    @PostMapping
    public ResponseEntity<Object> newTournament(@RequestBody @Valid NewTournament request) {
        tournamentService.createTournament(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "Tournament created successfully");
    }

    @Operation(summary = "Get tournaments including all status", description = "Retrieve a paginated list of all tournaments.")
    @GetMapping
    public ResponseEntity<Object> getAllTournaments(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "status") TournamentStatusFilter status,
            @RequestParam(name = "sortBy") TournamentSort sortBy,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "All tournaments retrieved successfully",
                tournamentService.getAllTournaments(pageNo, pageSize, sortBy, keyword, status));
    }

    @Operation(summary = "Get tournaments by id including all status", description = "Retrieve a paginated list of tournaments by their status.")
    @GetMapping("/{tournamentId}")
    public ResponseEntity<Object> getTournament(@PathVariable Long tournamentId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Tournament retrieved successfully", tournamentService.getTournament(tournamentId));
    }

    @Operation(summary = "Terminate a tournament, this api will force delete tournament if needed", description = "Terminate a tournament by its ID.")
    @PutMapping("/terminate/{tournamentId}")
    public ResponseEntity<Object> terminateTournament(@PathVariable Long tournamentId) {
        tournamentService.terminateTournament(tournamentId);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Tournament status updated successfully");
    }

    @Operation(summary = "Update a tournament", description = "Update the details of an existing tournament.")
    @PutMapping("/{tournamentId}")
    public ResponseEntity<Object> editTournament(@PathVariable Long tournamentId, @RequestBody @Valid EditTournament request) {
        tournamentService.editTournament(tournamentId, request);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Tournament updated successfully");
    }

    @Operation(summary = "Extend a tournament end date when needed", description = "Extend the schedule of an existing tournament. Example: 2025-04-02T08:01:00")
    @PutMapping("/extend/{tournamentId}")
    public ResponseEntity<Object> extendTournament(@PathVariable Long tournamentId, @RequestParam("newEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newEndDate) {
        tournamentService.extendTournamentEndDate(tournamentId, newEndDate);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Tournament schedule updated successfully");
    }

    @Operation(summary = "Get Teams by Tournament ID", description = "Fetch all teams associated with a specific tournament ID.")
    @GetMapping("/teams/{tournamentId}")
    public ResponseEntity<?> getTeamsByTournamentId(@PathVariable Long tournamentId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Teams retrieved successfully", tournamentService.getTeamsByTournamentId(tournamentId));
    }

    @Operation(summary = "Register a team to a tournament", description = "Registers a team and its members to a specific tournament. Ensures team is active and the tournament can accept more teams.")
    @PostMapping("/register-team/{tournamentId}/{teamId}")
    public ResponseEntity<Object> registerTeamToTournament(@PathVariable Long tournamentId, @PathVariable Long teamId) {
        tournamentService.registerTeamToTournament(tournamentId, teamId);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Team successfully registered to the tournament.");
    }

    @Operation(summary = "Get tournaments by team ID", description = "Retrieve all tournaments associated with a specific team.")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<Object> getTournamentsByTeam(@PathVariable Long teamId) {
        List<TournamentByTeamResponse> tournaments = tournamentService.getTournamentsByTeamId(teamId);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Tournaments fetched successfully", tournaments);
    }

    @Operation(summary = "Get dashboard data", description = "Fetches dashboard data for tournaments within a specified date range. Example: 2025-04-02T08:01:00")
    @GetMapping("/dashboard")
    public ResponseEntity<Object> getDashboard(@RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                               @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Dashboard data retrieved successfully", tournamentService.getDashboardByRange(startDate, endDate));
    }

    @Operation(summary = "Get monthly dashboard data", description = "Fetches monthly dashboard data for tournaments within a specified date range. Example: 2025-04-02T08:01:00 - 2025-04-22T08:01:00")
    @GetMapping("/dashboard/monthly")
    public ResponseEntity<Object> getMonthlyDashboard(@RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                      @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Monthly dashboard data retrieved successfully", tournamentService.getMonthlyDashboardByRange(startDate, endDate));
    }

}
