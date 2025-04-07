package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.MatchTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchTeamRepository extends JpaRepository<MatchTeam, Long> {

    List<MatchTeam> findByTeamId(Long id);

    Optional<MatchTeam> findByTeamIdAndMatchId(Long teamId, Long matchId);

    List<MatchTeam> findByMatchId(Long matchId);

    Optional<MatchTeam> findByTeamIdAndMatchIdAndAccountAccountId(Long id, Long id1, UUID accountId);
}
