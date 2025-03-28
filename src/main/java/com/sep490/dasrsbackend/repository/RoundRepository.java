package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long>, JpaSpecificationExecutor<Round> {
    List<Round> findByTournamentIdAndStatus(Long tournamentId, RoundStatus roundStatus);
    List<Round> findByTournamentId(Long tournamentId);
    @Query("SELECT r FROM Round r WHERE r.tournament.id = :id AND (r.status = 'PENDING' OR r.status = 'ACTIVE')")
    List<Round> findAvailableRoundByTournamentId(@Param("id") Long id);

    Optional<Round> findByStatusAndStartDateBefore(RoundStatus roundStatus, Date date);

    Optional<Round> findByStatusAndEndDateBefore(RoundStatus roundStatus, Date date);
}
