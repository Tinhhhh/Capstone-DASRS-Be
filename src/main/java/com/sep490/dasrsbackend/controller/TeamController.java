package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.response.TeamMemberResponse;
import com.sep490.dasrsbackend.service.MatchService;
import com.sep490.dasrsbackend.service.TeamService;
import com.sep490.dasrsbackend.model.payload.response.TeamResponse;
import com.sep490.dasrsbackend.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Object> removeMember(@RequestParam Long teamId, @RequestParam UUID memberId) {
        teamService.removeMember(teamId, memberId);
        return ResponseBuilder.responseBuilder(
                HttpStatus.OK,
                "Member successfully removed from the team"
        );
    }
    @PutMapping("/unlock-members")
    public ResponseEntity<Object> unlockMember(@RequestParam Long teamId, @RequestParam Long memberId) {
        teamService.unlockMember(teamId, memberId);
        return ResponseBuilder.responseBuilder(
                HttpStatus.OK,
                "Member successfully unlocked"
        );
    }

//    @PostMapping("/{teamId}/match-participants")
//    public ResponseEntity<Void> selectMatchParticipants(@PathVariable Long teamId, @RequestBody List<Long> memberIds) {
//        teamService.selectMatchParticipants(teamId, memberIds);
//        return ResponseEntity.ok().build();
//    }

//    @GetMapping("/get-matches")
//    public ResponseEntity<Object> getMatches(@RequestParam Long teamId) {
//        return ResponseBuilder.responseBuilderWithData(
//                HttpStatus.OK, "Successfully retrieved matches",
//                teamService.getMatches(teamId));
//    }

//    @PutMapping("/assign-member")
//    public ResponseEntity<Object> assignMemberToMatch(@RequestParam Long teamId, @RequestParam Long matchId, @RequestParam UUID assigner, @RequestParam UUID assignee) {
//        teamService.assignMemberToMatch(teamId, matchId, assigner, assignee);
//        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully assigned member to match");
//    }

    @GetMapping("/members/{teamId}")
    public ResponseEntity<Object> getTeamMembers(@PathVariable Long teamId) {
        List<TeamMemberResponse> teamMembers = teamService.getTeamMembers(teamId);
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK,
                "Team members retrieved successfully",
                teamMembers
        );
    }

    @PutMapping("/leadership/{teamId}/{newLeaderId}")
    public ResponseEntity<Object> transferLeadership(
            @PathVariable Long teamId,
            @PathVariable UUID newLeaderId) {
        teamService.transferLeadership(teamId, newLeaderId);
        return ResponseBuilder.responseBuilder(
                HttpStatus.OK,
                "Leadership transferred successfully"
        );
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<Object> getTeamDetails(@PathVariable Long teamId) {
        TeamResponse teamDetails = teamService.getTeamDetails(teamId);
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK,
                "Team details retrieved successfully",
                teamDetails
        );
    }

    @GetMapping
    public ResponseEntity<Object> getAllTeams() {
        List<TeamResponse> teams = teamService.getAllTeams();
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK,
                "All teams retrieved successfully",
                teams
        );
    }

    @PostMapping("/create")
    @Operation(summary = "Create a team", description = "Allows a player to create a new team. The player automatically becomes the team leader.")
    public ResponseEntity<Object> createTeam(
            @RequestParam UUID playerId,
            @RequestParam String teamName,
            @RequestParam String teamTag) {
        teamService.createTeam(playerId, teamName, teamTag);
        return ResponseBuilder.responseBuilder(
                HttpStatus.CREATED,
                "Team successfully created"
        );
    }

    @Operation(summary = "Player joins a team", description = "Allows a player to join a team, provided the team is not currently in a tournament and the player is not already part of another team.")
    @PostMapping("/{teamId}/join")
    public ResponseEntity<Object> joinTeam(@PathVariable Long teamId, @RequestParam UUID playerId) {
        teamService.joinTeam(teamId, playerId);
        return ResponseEntity.ok("Player successfully joined the team");
    }

    @Operation(summary = "Player changes team", description = "Allows a player (except the leader) to change to another team, provided the new team is not currently in a tournament.")
    @PostMapping("/{teamId}/change")
    public ResponseEntity<Object> changeTeam(@PathVariable Long teamId, @RequestParam UUID playerId) {
        teamService.changeTeam(teamId, playerId);
        return ResponseEntity.ok("Player successfully changed teams");
    }

    @Operation(summary = "Player leaves a team", description = "Allows a player to leave their current team, provided the team is not currently in a tournament.")
    @DeleteMapping("/{teamId}/leave")
    public ResponseEntity<Object> leaveTeam(@PathVariable Long teamId, @RequestParam UUID playerId) {
        teamService.leaveTeam(teamId, playerId);
        return ResponseEntity.ok("Player successfully left the team");
    }

    @DeleteMapping("/delete-team/{teamId}")
    @Operation(summary = "Delete team", description = "Allows the leader to delete the team. The team and all its related associations will be removed.")
    public ResponseEntity<Object> deleteTeam(@PathVariable Long teamId, @RequestParam UUID leaderId) {
        teamService.deleteTeam(teamId, leaderId);
        return ResponseBuilder.responseBuilder(
                HttpStatus.OK,
                "Team and related data successfully deleted."
        );
    }

}
