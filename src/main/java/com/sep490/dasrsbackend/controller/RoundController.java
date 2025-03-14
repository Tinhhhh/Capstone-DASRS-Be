package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.enums.RoundSort;
import com.sep490.dasrsbackend.model.enums.TournamentSort;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.EditRound;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.service.RoundService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping
    public ResponseEntity<Object> editRound(@RequestBody @Valid EditRound request) {
        roundService.editRound(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "Round edited successfully");
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

}
