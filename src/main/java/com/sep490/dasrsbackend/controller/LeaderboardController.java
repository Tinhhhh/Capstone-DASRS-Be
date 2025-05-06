package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.service.LeaderboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leaderboards")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Leaderboards required to ranking every teams in round, tournament.")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/round/{roundId}")
    public ResponseEntity<Object> getLeaderboardsByRoundId(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection,
            @PathVariable Long roundId
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved all leaderboards", leaderboardService.getLeaderboardByRoundId(roundId, pageNo, pageSize, sortBy, sortDirection));
    }

    @GetMapping("/rounds/{roundId}")
    public ResponseEntity<Object> getLeaderboardsByRoundIdWithMatchDetails(
            @RequestParam(name = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = "ranking", required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "ASC", required = false) String sortDirection,
            @PathVariable Long roundId
    ) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK,
                "Successfully retrieved leaderboard with match details for round",
                leaderboardService.getLeaderboardWithMatchDetails(roundId, pageNo, pageSize, sortBy, sortDirection)
        );
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<Object> getLeaderboardsByTournamentId(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection,
            @PathVariable Long tournamentId
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved all leaderboards", leaderboardService.getLeaderboardByTournamentId(tournamentId, pageNo, pageSize, sortBy, sortDirection));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<Object> getLeaderboardsByTeamId(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection,
            @PathVariable Long teamId
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved all leaderboards", leaderboardService.getLeaderboardByTeamId(teamId, pageNo, pageSize, sortBy, sortDirection));
    }

}
