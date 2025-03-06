package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.request.EditRound;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.model.payload.response.ListRound;
import com.sep490.dasrsbackend.model.payload.response.RoundResponse;

public interface RoundService {
    void newRound(NewRound newRound);
//    void editRound(NewRound newRound);
    RoundResponse findRoundByRoundId(Long id);
    ListRound findRoundByTournamentId(Long id);

    void editRound(EditRound request);
}
