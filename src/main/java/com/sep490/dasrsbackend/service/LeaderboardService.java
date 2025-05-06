package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.response.LeaderboardData;
import com.sep490.dasrsbackend.model.payload.response.LeaderboardResponseForRound;
import com.sep490.dasrsbackend.model.payload.response.LeaderboardResponseForTournament;
import com.sep490.dasrsbackend.model.payload.response.LeaderboardWithMatchDetailsResponse;

import java.util.List;

public interface LeaderboardService {
    void updateLeaderboard(Long roundId);

    LeaderboardResponseForRound getLeaderboardByRoundId(Long roundId, int pageNo, int pageSize, String sortBy, String sortDir);

    List<LeaderboardData> getLeaderboardByTeamId(Long teamId, int pageNo, int pageSize, String sortBy, String sortDir);

    LeaderboardResponseForTournament getLeaderboardByTournamentId(Long tournamentId, int pageNo, int pageSize, String sortBy, String sortDir);

    LeaderboardWithMatchDetailsResponse getLeaderboardWithMatchDetails(Long roundId, int pageNo, int pageSize, String sortBy, String sortDir);

}
