package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.response.MatchResponse;
import com.sep490.dasrsbackend.model.payload.response.TeamMemberResponse;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    List<MatchResponse> getMatches(Long teamId);
    void complainAboutMatch(Long teamId, String complaint);
    void removeMember(Long teamId, Long memberId);
//    void selectMatchParticipants(Long teamId, List<Long> memberIds);
    List<TeamMemberResponse> getTeamMembers(Long teamId);
    void transferLeadership(Long teamId, Long newLeaderId);
    void assignMemberToMatch(Long teamId, Long matchId, UUID assigner, UUID assignee);
    void unlockMember(Long teamId, Long memberId);
}
