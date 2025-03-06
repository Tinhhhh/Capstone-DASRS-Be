package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Leaderboard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {
    @Query("SELECT l FROM Leaderboard l ORDER BY l.ranking ASC")
    List<Leaderboard> findTopNLeaderboard(Pageable pageable);
}
