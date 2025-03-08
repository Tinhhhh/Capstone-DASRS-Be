package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.MatchTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchTeamRepository extends JpaRepository<MatchTeam, Long> {

    List<MatchTeam> findByTeamId(Long id);

    Optional<MatchTeam> findByTeamIdAndMatchId(Long teamId, Long matchId);
}
