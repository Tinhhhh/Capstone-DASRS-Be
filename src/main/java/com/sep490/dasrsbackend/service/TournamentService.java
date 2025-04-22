package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.enums.TournamentSort;
import com.sep490.dasrsbackend.model.enums.TournamentStatusFilter;
import com.sep490.dasrsbackend.model.payload.request.EditTournament;
import com.sep490.dasrsbackend.model.payload.request.NewTournament;
import com.sep490.dasrsbackend.model.payload.response.*;

import java.time.LocalDateTime;
import java.util.List;

public interface TournamentService {
    void createTournament(NewTournament newTournament);

    void editTournament(Long tournamentId, EditTournament editTournament);

    ListTournament getAllTournaments(int pageNo, int pageSize, TournamentSort sortBy, String keyword, TournamentStatusFilter status);

    TournamentResponse getTournament(Long id);

    void terminateTournament(Long id);

    void extendTournamentEndDate(Long id, LocalDateTime endDate);

    List<TeamTournamentDetails> getTeamsByTournamentId(Long tournamentId);

    void registerTeamToTournament(Long tournamentId, Long teamId);

    List<TournamentByTeamResponse> getTournamentsByTeamId(Long teamId);

    TodayDashboard getDashboardByRange(LocalDateTime startDate, LocalDateTime endDate);

    MonthlyDashboard getMonthlyDashboardByRange(LocalDateTime startDate, LocalDateTime endDate);


}
