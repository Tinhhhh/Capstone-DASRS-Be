package com.sep490.dasrsbackend.controller;

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

    @PutMapping("/edit")
    public ResponseEntity<Object> editRound(@RequestBody @Valid EditRound request) {
        roundService.editRound(request);
        return ResponseBuilder.responseBuilder(HttpStatus.CREATED, "Round edited successfully");
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getRound(@RequestParam Long roundId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved data ", roundService.findRoundByRoundId(roundId));
    }

//    @GetMapping("/get/all")
//    public ResponseEntity<Object> getAllTournamentRound(
//           @RequestParam long tournamentId
//    ) {
//        return ResponseBuilder.responseBuilderWithData(
//                HttpStatus.OK, "Successfully retrieved scored methods",
//                environmentService.getAllEnvironment(pageNo, pageSize, sortBy, sortDirection));
//    }

}
