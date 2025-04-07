package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.request.ChangeMatchSlot;
import com.sep490.dasrsbackend.model.payload.request.MatchScoreData;
import com.sep490.dasrsbackend.model.payload.request.MatchCarData;
import com.sep490.dasrsbackend.model.payload.response.MatchResponse;
import com.sep490.dasrsbackend.model.payload.response.MatchResponseForTeam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MatchService {
    List<MatchResponseForTeam> getMatches(Long teamId);

    void assignMemberToMatch(Long teamId, Long matchId, UUID assigner, UUID assignee);

    List<MatchResponse> getMatchByRoundId(Long roundId);

    void updateMatchTeamScore(MatchScoreData matchScoreData);

    void updateMatchTeamCar(MatchCarData matchCarData);

    void changeMatchSlot(Long MatchId, ChangeMatchSlot changeMatchSlot);

    List<MatchResponse> getMatchesByTournamentId(Long tournamentId);

    MatchResponse getAvailableMatch(LocalDateTime date);

    List<MatchResponse> getMatchByRoundIdAndPlayerId(Long roundId, UUID accountId);
}
