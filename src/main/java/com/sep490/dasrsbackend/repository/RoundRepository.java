package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    List<Round> findByTournamentIdAndStatus(Long tournamentId, RoundStatus roundStatus);
    List<Round> findByTournamentId(Long tournamentId);
}
