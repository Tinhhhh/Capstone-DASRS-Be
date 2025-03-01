package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.request.NewTournament;

public interface TournamentService {
    void createTournament(NewTournament newTournament);
    void editTournament(NewTournament newTournament);

//    void getAllTournaments();
//    void getTournamentById(Long tournamentId);
}
