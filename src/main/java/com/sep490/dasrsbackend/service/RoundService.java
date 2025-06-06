package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.enums.RoundSort;
import com.sep490.dasrsbackend.model.enums.RoundStatusFilter;
import com.sep490.dasrsbackend.model.payload.request.EditRound;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.model.payload.response.GetRoundsByAccountResponse;
import com.sep490.dasrsbackend.model.payload.response.GetRoundsByTeamResponse;
import com.sep490.dasrsbackend.model.payload.response.ListRoundResponseDetails;
import com.sep490.dasrsbackend.model.payload.response.RoundResponseDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RoundService {
    void newRound(NewRound newRound);

    RoundResponseDetails findRoundByRoundId(Long id);

    List<RoundResponseDetails> findRoundByTournamentId(Long id);

    ListRoundResponseDetails findAllRounds(int pageNo, int pageSize, RoundSort sortBy, String keyword, RoundStatusFilter status);

    void editRound(EditRound request);

    void terminateRound(Long id);

    GetRoundsByAccountResponse getRoundsByAccountId(UUID accountId, int pageNo, int pageSize, RoundSort sortBy, String keyword);

    //api for landing page
    ListRoundResponseDetails findAllRoundsByDate(int pageNo, int pageSize, RoundSort sortBy, String keyword, LocalDateTime start, LocalDateTime end);

    void injectTeamToTournament(Long tournamentId, Long teamId);

    GetRoundsByTeamResponse getRoundsByTeamIdAndTournamentId(Long teamId, Long tournamentId, int pageNo, int pageSize, RoundSort sortBy, String keyword);

    void extendRoundEndDate(Long id, LocalDateTime endDate);
}
