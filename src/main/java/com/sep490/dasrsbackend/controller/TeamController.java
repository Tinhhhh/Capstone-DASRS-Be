package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.service.TeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Team method for player.")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/get-matches")
    public ResponseEntity<Object> getMatches(@RequestParam Long teamId) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Successfully retrieved matches",
                teamService.getMatches(teamId));
    }

    @PutMapping("/assign-member")
    public ResponseEntity<Object> assignMemberToMatch(@RequestParam Long teamId, @RequestParam Long matchId, @RequestParam UUID assigner, @RequestParam UUID assignee) {
        teamService.assignMemberToMatch(teamId, matchId, assigner, assignee);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully assigned member to match");
    }

}
