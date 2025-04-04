package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.enums.TeamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findById(Long id);
    Optional<Team> findByTeamNameAndTeamTag(String teamName, String teamTag);
    List<Team> getTeamByTournamentIdAndStatus(Long tournamentId, TeamStatus teamStatus);
    List<Team> getTeamByTournamentId(Long id);

    List<Team> findByStatus(TeamStatus teamStatus);
}
