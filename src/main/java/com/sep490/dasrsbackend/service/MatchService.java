package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.response.MatchResponse;

import java.util.List;
import java.util.UUID;

public interface MatchService {
    List<MatchResponse> getMatches(Long teamId);

    void assignMemberToMatch(Long teamId, Long matchId, UUID assigner, UUID assignee);

    List<MatchResponse> getMatchByRoundId(Long roundId);

}
