package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.entity.TournamentTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {

    List<TournamentTeam> findByTournamentId(Long tournamentId);

    List<TournamentTeam> findByTeamAndTournamentNotNull(Team team);

    void deleteAllByTeam(Team team);

    List<TournamentTeam> findByTeam(Team team);

    @Query("SELECT tt.tournament FROM TournamentTeam tt WHERE tt.team.id = :teamId AND tt.tournament.status = 'ACTIVE'")
    List<Tournament> findActiveTournamentsByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT CASE WHEN COUNT(tt) > 0 THEN TRUE ELSE FALSE END FROM TournamentTeam tt WHERE tt.team = :team")
    boolean existsByTeam(@Param("team") Team team);

    @Query("SELECT tt.tournament FROM TournamentTeam tt WHERE tt.team.id = :teamId")
    List<Tournament> findTournamentsByTeamId(Long teamId);

    boolean existsByTournamentIdAndAccount_AccountId(Long tournamentId, UUID accountId);
}
