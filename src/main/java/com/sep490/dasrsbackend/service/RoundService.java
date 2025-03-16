package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.enums.RoundSort;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import com.sep490.dasrsbackend.model.payload.request.EditRound;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.model.payload.response.RoundResponse;

import java.util.List;

public interface RoundService {
    void newRound(NewRound newRound);

    //    void editRound(NewRound newRound);
    RoundResponse findRoundByRoundId(Long id);

    List<RoundResponse> findRoundByTournamentId(Long id);

    List<RoundResponse> findAllRounds(int pageNo, int pageSize, RoundSort sortBy, String keyword);

    void editRound(EditRound request);

    void changeRoundStatus(Long id, RoundStatus status);
}
