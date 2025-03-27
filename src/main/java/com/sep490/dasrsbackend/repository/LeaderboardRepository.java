package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Leaderboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {
    @Query("SELECT l FROM Leaderboard l WHERE l.round.id = :id ORDER BY l.ranking ASC")
    List<Leaderboard> findTopNLeaderboard(@Param("id") Long id);

    @Query("SELECT l FROM Leaderboard l WHERE l.round.id = :roundId AND l.team.isDisqualified = false")
    Page<Leaderboard> findByRoundId(@Param("roundId") Long roundId, Pageable pageable);

    @Query("SELECT l FROM Leaderboard l WHERE l.team.id = :teamId AND l.team.isDisqualified = false")
    Page<Leaderboard> findByTeamId(@Param("teamId") Long teamId, Pageable pageable);

    @Query("SELECT l FROM Leaderboard l " +
            "JOIN l.team t " +
            "JOIN l.round r " +
            "WHERE r.id = :id AND t.isDisqualified = false")
    List<Leaderboard> findByRoundIdNotDisqualified(@Param("id") Long id);

    List<Leaderboard> findByRoundId(Long roundId);
}
