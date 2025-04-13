package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.enums.RoundSort;
import com.sep490.dasrsbackend.model.payload.request.EditRound;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.model.payload.response.GetRoundsByAccountResponse;
import com.sep490.dasrsbackend.model.payload.response.ListRoundResponse;
import com.sep490.dasrsbackend.model.payload.response.RoundResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RoundService {
    void newRound(NewRound newRound);

    RoundResponse findRoundByRoundId(Long id);

    List<RoundResponse> findRoundByTournamentId(Long id);

    ListRoundResponse findAllRounds(int pageNo, int pageSize, RoundSort sortBy, String keyword);

    void editRound(EditRound request);

    void terminateRound(Long id);

    GetRoundsByAccountResponse getRoundsByAccountId(UUID accountId, int pageNo, int pageSize, RoundSort sortBy, String keyword);

    //api for landing page
    ListRoundResponse findAllRoundsByDate(int pageNo, int pageSize, RoundSort sortBy, String keyword, LocalDateTime start, LocalDateTime end);

    void injectTeamToTournament(Long tournamentId, Long teamId);
}
