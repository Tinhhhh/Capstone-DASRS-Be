package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.enums.TournamentSort;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import com.sep490.dasrsbackend.model.payload.request.EditTournament;
import com.sep490.dasrsbackend.model.payload.request.NewTournament;
import com.sep490.dasrsbackend.model.payload.response.ListTournament;
import com.sep490.dasrsbackend.model.payload.response.TournamentResponse;

import java.util.List;

public interface TournamentService {
    void createTournament(NewTournament newTournament);

    void editTournament(Long tournamentId, EditTournament editTournament);

    ListTournament getAllTournaments(int pageNo, int pageSize, TournamentSort sortBy, String keyword);

    TournamentResponse getTournament(Long id);

    void startTournament(Long id);

    void changeStatus(Long id, TournamentStatus status);

    void editTournamentSchedule(Long id, int day);

    List<Account> getUsersByTournament(Long tournamentId);
}
