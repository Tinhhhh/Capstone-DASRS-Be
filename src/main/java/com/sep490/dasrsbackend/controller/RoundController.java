package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.enums.RoundSort;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.EditRound;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.model.payload.response.GetRoundsByAccountResponse;
import com.sep490.dasrsbackend.service.RoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rounds")
@RequiredArgsConstructor
@Tag(name = "Round", description = "Round required to use to create tournament.")
public class RoundController {

    private final RoundService roundService;

    @PostMapping
    public ResponseEntity<Object> newRound(@RequestBody @Valid NewRound request) {
        roundService.newRound(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "New round created successfully");
    }

    @Operation(summary = "Edit round, can be edit if the round is latest", description = "Edit the round with the specified ID and update its details.")
    @PutMapping
    public ResponseEntity<Object> editRound(@RequestBody @Valid EditRound request) {
        roundService.editRound(request);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Round edited successfully");
    }

    @GetMapping("/{roundId}")
    public ResponseEntity<Object> getRound(@PathVariable Long roundId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved data ", roundService.findRoundByRoundId(roundId));
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<Object> getRoundByTournament(@PathVariable Long tournamentId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved data ", roundService.findRoundByTournamentId(tournamentId));
    }

    @GetMapping
    public ResponseEntity<Object> getAllRounds(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy") RoundSort sortBy,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved data",
                roundService.findAllRounds(pageNo, pageSize, sortBy, keyword));
    }

    @Operation(summary = "Terminate round", description = "Terminate the round with the specified ID and update its status.")
    @PutMapping("/terminate/{roundId}")
    public ResponseEntity<Object> terminateRound(@PathVariable Long roundId) {
        roundService.terminateRound(roundId);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Round terminated successfully");
    }

    @Operation(summary = "Get rounds by account ID", description = "Retrieve rounds for the specified account with pagination, sorting, and optional search by round name or tournament name")
    @GetMapping("/player/rounds")
    public ResponseEntity<Object> getRoundsByAccountId(
            @RequestParam(name = "accountId") UUID accountId,
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy") RoundSort sortBy,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        GetRoundsByAccountResponse roundsResponse = roundService.getRoundsByAccountId(accountId, pageNo, pageSize, sortBy, keyword);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Rounds retrieved successfully.", roundsResponse);
    }

    @Operation(summary = "Get rounds landing page", description = "Retrieve rounds for the landing page with pagination, sorting, and optional search by round name or tournament name." +
            "format: yyyy-MM-ddTHH:mm:ss. Example: 2025-04-02T08:01:00")
    @GetMapping("/landing")
    public ResponseEntity<Object> getRoundsLandingPage(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy") RoundSort sortBy,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved data",
                roundService.findAllRoundsByDate(pageNo, pageSize, sortBy, keyword, startDate, endDate));
    }

    @Operation(summary = "This API is a test case when new team join in tournament", description = "A team join in a tournament, and the team will be injected to the round of tournament")
    @PutMapping("/join/{tournamentId}/{teamId}")
    public ResponseEntity<Object> injectTeamToRound(@PathVariable Long tournamentId, @PathVariable Long teamId) {
        roundService.injectTeamToTournament(tournamentId, teamId);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Team injected successfully");
    }

    @Operation(summary = "Extend round end date", description = "Extend the end date of the round with the specified ID.")
    @PutMapping("/extend/{roundId}")
    public ResponseEntity<Object> extendRoundEndDate(@PathVariable Long roundId, @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        roundService.extendRoundEndDate(roundId, endDate);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Round end date extended successfully");
    }

}
