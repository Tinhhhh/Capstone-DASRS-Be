package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.response.TeamResponse;
import com.sep490.dasrsbackend.service.TeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Team method for player.")
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/complain/{teamId}")
    public ResponseEntity<Void> complainAboutMatch(@PathVariable Long teamId, @RequestParam String complaint) {
        teamService.complainAboutMatch(teamId, complaint);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove-members")
    public ResponseEntity<Void> removeMember(@RequestParam Long teamId, @RequestParam Long memberId) {
        teamService.removeMember(teamId, memberId);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/{teamId}/match-participants")
//    public ResponseEntity<Void> selectMatchParticipants(@PathVariable Long teamId, @RequestBody List<Long> memberIds) {
//        teamService.selectMatchParticipants(teamId, memberIds);
//        return ResponseEntity.ok().build();
//    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamResponse>> getTeamMembers(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeamMembers(teamId));
    }

    @PutMapping("/leadership/{teamId}/{newLeaderId}")
    public ResponseEntity<Void> transferLeadership(@PathVariable Long teamId, @PathVariable Long newLeaderId) {
        teamService.transferLeadership(teamId, newLeaderId);
        return ResponseEntity.ok().build();
    }
}
