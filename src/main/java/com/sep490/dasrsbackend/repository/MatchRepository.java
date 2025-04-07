package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByRoundId(Long id);

    Match findByTimeStartAndStatus(Date timeStart, MatchStatus status);

    @Query("SELECT m FROM Match m WHERE m.round.tournament.id = :tournamentId")
    List<Match> findAllByTournamentId(Long tournamentId);

    @Query(value = "SELECT * FROM match " +
            "WHERE date_trunc('hour', time_start) = date_trunc('hour', CAST(:time AS TIMESTAMP))",
            nativeQuery = true)
    Match findMatchByHour(@Param("time") String time);

    @Query("SELECT m FROM Match m JOIN m.matchTeamList mt " +
            "WHERE m.round.id = :roundId AND mt.account.accountId = :accountId")
    List<Match> findMatchesByRoundIdAndAccountId(@Param("roundId") Long roundId, @Param("accountId") UUID accountId);
}
