package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.payload.response.MatchResponse;
import com.sep490.dasrsbackend.model.payload.response.TeamMemberResponse;
import com.sep490.dasrsbackend.model.payload.response.TeamResponse;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    List<MatchResponse> getMatches(Long teamId);

    void complainAboutMatch(Long teamId, String complaint);

    void removeMember(Long teamId, UUID memberId);

//    void selectMatchParticipants(Long teamId, List<Long> memberIds);

    List<TeamMemberResponse> getTeamMembers(Long teamId);

    void transferLeadership(Long teamId, UUID newLeaderId);

//    void assignMemberToMatch(Long teamId, Long matchId, UUID assigner, UUID assignee);

    void unlockMember(Long teamId, Long memberId);

    TeamResponse getTeamDetails(Long teamId);

    List<TeamResponse> getAllTeams();

    void joinTeam(Long teamId, UUID playerId);

    void changeTeam(Long teamId, UUID playerId);

    void leaveTeam(Long teamId, UUID playerId);

    void deleteTeam(Long teamId, UUID leaderId);

    void createTeam(UUID playerId, String teamName, String teamTag);
}
