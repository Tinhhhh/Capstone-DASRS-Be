package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByRoundId(Long id);

    Match findByTimeStartAndStatus(Date timeStart, MatchStatus status);

    @Query("SELECT m FROM Match m WHERE m.round.tournament.id = :tournamentId")
    List<Match> findAllByTournamentId(Long tournamentId);
}
