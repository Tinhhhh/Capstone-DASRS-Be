package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
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
public interface RoundRepository extends JpaRepository<Round, Long>, JpaSpecificationExecutor<Round> {
    List<Round> findByTournamentIdAndStatus(Long tournamentId, RoundStatus roundStatus);

    List<Round> findByTournamentId(Long tournamentId);

    @Query("SELECT r FROM Round r WHERE r.tournament.id = :id AND  r.status = 'ACTIVE'")
    List<Round> findValidRoundByTournamentId(@Param("id") Long id);

    @Query("SELECT r FROM Round r WHERE r.tournament.id = :id AND (r.status = 'ACTIVE' OR r.status = 'COMPLETED')")
    List<Round> findAvailableRoundByTournamentId(@Param("id") Long id);

    Optional<Round> findByStatusAndStartDateBefore(RoundStatus roundStatus, Date date);

    Optional<Round> findByStatusAndEndDateBefore(RoundStatus roundStatus, Date date);

    @Query("SELECT DISTINCT r FROM Round r " +
            "JOIN r.matchList m " +
            "JOIN m.matchTeamList mt " +
            "JOIN mt.account a " +
            "WHERE a.accountId = :accountId")
    List<Round> findRoundsByAccountId(@Param("accountId") UUID accountId);

    Optional<Round> findByMatchTypeId(Long id);

    Optional<Round> findByScoredMethodId(Long scoredMethodId);
}
