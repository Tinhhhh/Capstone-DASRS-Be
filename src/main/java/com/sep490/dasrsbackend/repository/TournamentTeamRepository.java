package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.entity.TournamentTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {

    List<TournamentTeam> findByTournamentId(Long tournamentId);

    List<TournamentTeam> findByTeamAndTournamentNotNull(Team team);
}
