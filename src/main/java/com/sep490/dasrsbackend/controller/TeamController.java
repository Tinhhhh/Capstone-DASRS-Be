package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.payload.response.TeamResponse;
import com.sep490.dasrsbackend.service.TeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Team method for player.")
public class TeamController {
    @Autowired
    private TeamService teamService;

    @PostMapping("/{teamId}/complain")
    public ResponseEntity<Void> complainAboutMatch(@PathVariable Long teamId, @RequestParam String complaint) {
        teamService.complainAboutMatch(teamId, complaint);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        teamService.removeMember(teamId, memberId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/match-participants")
    public ResponseEntity<Void> selectMatchParticipants(@PathVariable Long teamId, @RequestBody List<Long> memberIds) {
        teamService.selectMatchParticipants(teamId, memberIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamResponse>> getTeamMembers(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeamMembers(teamId));
    }

    @PutMapping("/{teamId}/transfer-leadership/{newLeaderId}")
    public ResponseEntity<Void> transferLeadership(@PathVariable Long teamId, @PathVariable Long newLeaderId) {
        teamService.transferLeadership(teamId, newLeaderId);
        return ResponseEntity.ok().build();
    }
}
