package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.enums.MatchSort;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.ChangeMatchSlot;
import com.sep490.dasrsbackend.model.payload.request.MatchCarData;
import com.sep490.dasrsbackend.model.payload.request.MatchScoreData;
import com.sep490.dasrsbackend.model.payload.request.UnityRoomRequest;
import com.sep490.dasrsbackend.model.payload.response.MatchResponse;
import com.sep490.dasrsbackend.service.MatchService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Tag(name = "Match", description = "Match required to use for round.")
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/team/{teamId}")
    public ResponseEntity<Object> getMatchesByTeam(
            @PathVariable Long teamId) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved matches",
                matchService.getMatches(teamId));
    }

    @PutMapping("/assign/{matchTeamId}")
    public ResponseEntity<Object> assignMemberToMatch(@PathVariable Long matchTeamId, @RequestParam UUID assigner, @RequestParam UUID assignee) {
        matchService.assignMemberToMatch(matchTeamId, assigner, assignee);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully assigned member to match");
    }

    @GetMapping("/round/{roundId}")
    public ResponseEntity<Object> getMatchByRoundId(@RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                    @RequestParam(name = "sortBy") MatchSort sortBy,
                                                    @PathVariable Long roundId,
                                                    @RequestParam(name = "keyword", required = false) String keyword) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved matches",
                matchService.getMatchByRoundId(pageNo, pageSize, sortBy, roundId, keyword));
    }

    @PutMapping("/slot/{matchId}")
    public ResponseEntity<Object> changeMatchSlot(@PathVariable Long matchId, @RequestBody @Valid ChangeMatchSlot changeMatchSlot) {
        matchService.changeMatchSlot(matchId, changeMatchSlot);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully change match slot");
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<Object> getMatchesByTournamentId(@PathVariable Long tournamentId) {
        try {
            List<MatchResponse> matches = matchService.getMatchesByTournamentId(tournamentId);
            return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Matches retrieved successfully", matches);
        } catch (DasrsException e) {
            return ResponseBuilder.responseBuilder(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ResponseBuilder.responseBuilder(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Operation(summary = "Update match score data after finish a match")
    @PutMapping("/score-data")
    public ResponseEntity<Object> retrieveMatchData(@RequestBody @Valid MatchScoreData match) {
        matchService.updateMatchTeamScore(match);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully update match score data");
    }

    @Operation(summary = "Update match car data after finish a match")
    @PutMapping("/car-data")
    public ResponseEntity<Object> updateMatchDataDetails(@RequestBody @Valid MatchCarData match) {
        matchService.updateMatchTeamCar(match);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully updated match data details");
    }

    @Operation(summary = "Get match by time", description = "Get match by time, format: yyyy-MM-ddTHH:mm:ss. Example: 2025-04-02T08:01:00")
    @GetMapping("/available")
    public ResponseEntity<Object> getAvailableMatch(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved available match",
                matchService.getAvailableMatch(date));
    }

    @GetMapping("/by-round-and-player")
    @Operation(summary = "Get matches by round and player",
            description = "Returns only matches from a round where the player participated")
    public ResponseEntity<Object> getMatchesByRoundAndPlayer(@RequestParam Long roundId, @RequestParam UUID accountId) {
        List<MatchResponse> responses = matchService.getMatchByRoundIdAndPlayerId(roundId, accountId);
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Matches retrieved successfully", responses);
    }

    @GetMapping("/unity")
    public ResponseEntity<Object> isPlayerInUnity(@RequestParam("accountId") UUID accountId,
                                                  @RequestParam("roomId") String matchCode,
                                                  @RequestParam("joinTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime joinTime) {
        UnityRoomRequest unityRoomRequest = new UnityRoomRequest(accountId, matchCode, DateUtil.convertToDate(joinTime));

        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved data",
                matchService.isValidPlayerInMatch(unityRoomRequest));
    }

    @GetMapping("/score-details/{matchId}/{teamId}")
    public ResponseEntity<Object> getMatchScoreDetails(@PathVariable Long matchId, @PathVariable Long teamId) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved match score details",
                matchService.getMatchScoreDetails(matchId, teamId));
    }

}
