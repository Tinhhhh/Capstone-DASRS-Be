package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.enums.MatchSort;
import com.sep490.dasrsbackend.model.payload.request.ChangeMatchSlot;
import com.sep490.dasrsbackend.model.payload.request.MatchScoreData;
import com.sep490.dasrsbackend.model.payload.request.MatchCarData;
import com.sep490.dasrsbackend.model.payload.response.ListMatchResponse;
import com.sep490.dasrsbackend.model.payload.response.MatchResponse;
import com.sep490.dasrsbackend.model.payload.response.MatchResponseForTeam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MatchService {
    List<MatchResponseForTeam> getMatches(Long teamId);

    void assignMemberToMatch(Long teamId, Long matchId, UUID assigner, UUID assignee);

    ListMatchResponse getMatchByRoundId(int pageNo, int pageSize, MatchSort sortBy, Long roundId, String keyword);

    void updateMatchTeamScore(MatchScoreData matchScoreData);

    void updateMatchTeamCar(MatchCarData matchCarData);

    void changeMatchSlot(Long MatchId, ChangeMatchSlot changeMatchSlot);

    List<MatchResponse> getMatchesByTournamentId(Long tournamentId);

    MatchResponse getAvailableMatch(LocalDateTime date);

}
