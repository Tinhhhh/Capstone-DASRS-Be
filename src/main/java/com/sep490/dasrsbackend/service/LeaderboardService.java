package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.entity.Leaderboard;
import com.sep490.dasrsbackend.model.payload.response.ListLeaderboardResponse;

import java.util.List;

public interface LeaderboardService {
    void updateLeaderboard(Long roundId);

    ListLeaderboardResponse getLeaderboardByRoundId(Long roundId, int pageNo, int pageSize, String sortBy, String sortDir);

    ListLeaderboardResponse getLeaderboardByTeamId(Long teamId, int pageNo, int pageSize, String sortBy, String sortDir);

    ListLeaderboardResponse getLeaderboardByTournamentId(Long tournamentId, int pageNo, int pageSize, String sortBy, String sortDir);

}
