package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {
    List<Match> findByRoundId(Long id);

    Match findByTimeStartAndStatus(Date timeStart, MatchStatus status);

    @Query("SELECT m FROM Match m WHERE m.round.tournament.id = :tournamentId")
    List<Match> findAllByTournamentId(Long tournamentId);

    @Query(value = "SELECT * FROM match " +
            "WHERE date_trunc('hour', time_start) = date_trunc('hour', CAST(:time AS TIMESTAMP))",
            nativeQuery = true)
    Optional<Match> findMatchByHour(@Param("time") String time);

    @Query("SELECT m FROM Match m JOIN m.matchTeamList mt " +
            "WHERE m.round.id = :roundId AND mt.account.accountId = :accountId")
    List<Match> findMatchesByRoundIdAndAccountId(@Param("roundId") Long roundId, @Param("accountId") UUID accountId);

    Optional<Match> findByMatchCode(String matchCode);

    @Query("SELECT m FROM Match m WHERE :time between m.timeStart and m.timeEnd")
    List<Match> findByMatchByTime(@Param("time") Date time);

}
