package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.ChangeMatchSlot;
import com.sep490.dasrsbackend.model.payload.request.MatchScoreData;
import com.sep490.dasrsbackend.model.payload.request.MatchCarData;
import com.sep490.dasrsbackend.model.payload.response.MatchResponse;
import com.sep490.dasrsbackend.service.MatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Tag(name = "Match", description = "Match required to use for round.")
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/team/{teamId}")
    public ResponseEntity<Object> getMatchesByTeam(@PathVariable Long teamId) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved matches",
                matchService.getMatches(teamId));
    }

    @PutMapping("/assign/{teamId}/{matchId}")
    public ResponseEntity<Object> assignMemberToMatch(@PathVariable Long teamId, @PathVariable Long matchId, @RequestParam UUID assigner, @RequestParam UUID assignee) {
        matchService.assignMemberToMatch(teamId, matchId, assigner, assignee);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully assigned member to match");
    }

    @GetMapping("/round/{roundId}")
    public ResponseEntity<Object> getMatchByRoundId(@PathVariable Long roundId) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved matches",
                matchService.getMatchByRoundId(roundId));
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

    @PutMapping("/score-data")
    public ResponseEntity<Object> retrieveMatchData(@RequestBody @Valid MatchScoreData match) {
        matchService.updateMatchTeamScore(match);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully update match score data");
    }

    @PutMapping("/car-data")
    public ResponseEntity<Object> updateMatchDataDetails(@RequestBody @Valid MatchCarData match) {
        matchService.updateMatchTeamCar(match);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully updated match data details");
    }


}
