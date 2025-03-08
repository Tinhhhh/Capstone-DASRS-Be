package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.response.MatchResponse;
import com.sep490.dasrsbackend.model.payload.response.TeamResponse;

import java.util.List;

public interface TeamService {

    List<MatchResponse> getUpcomingMatches(Long teamId);
    void complainAboutMatch(Long teamId, String complaint);
    void removeMember(Long teamId, Long memberId);
    void selectMatchParticipants(Long teamId, List<Long> memberIds);
    List<TeamResponse> getTeamMembers(Long teamId);
    void transferLeadership(Long teamId, Long newLeaderId);

}
