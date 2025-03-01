package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.request.NewTournament;

public interface TournamentService {
    void createTournament(NewTournament newTournament);
    void editTournament(NewTournament newTournament);

    Object getAllTournaments(int pageNo, int pageSize, String sortBy, String sortDirection);

    Object getTournament(Long id);
}
