package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.enums.MatchSort;
import com.sep490.dasrsbackend.model.payload.request.MatchCarData;
import com.sep490.dasrsbackend.model.payload.request.MatchScoreData;
import com.sep490.dasrsbackend.model.payload.request.UnityRoomRequest;
import com.sep490.dasrsbackend.model.payload.response.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MatchService {
    List<MatchResponseForTeam> getMatches(Long teamId);

    void assignMemberToMatch(Long matchTeamId, UUID assigner, UUID assignee);

    ListMatchResponse getMatchByRoundId(int pageNo, int pageSize, MatchSort sortBy, Long roundId, String keyword);

    void updateMatchTeamScore(MatchScoreData matchScoreData);

    void updateMatchTeamCar(MatchCarData matchCarData);

    void changeMatchSlot(Long MatchId, LocalDateTime changeMatchSlot);

    List<MatchResponse> getMatchesByTournamentId(Long tournamentId);

    UnityMatchResponse getAvailableMatch(Long tournamentId, LocalDateTime date);

    UnityRoomResponse isValidPlayerInMatch(UnityRoomRequest unityRoomRequest);

    List<MatchResponse> getMatchByRoundIdAndPlayerId(Long roundId, UUID accountId);

    //get match score details
    List<LeaderboardDetails> getMatchScoreDetails(Long matchId, Long teamId);

    void rejoinMatch(Long matchId, UUID accountId);

    void createMatch(Long roundId, Long teamId);
}
