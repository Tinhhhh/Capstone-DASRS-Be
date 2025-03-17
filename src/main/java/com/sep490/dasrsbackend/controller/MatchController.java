package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.ChangeMatchSlot;
import com.sep490.dasrsbackend.service.MatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
