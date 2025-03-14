package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long>, JpaSpecificationExecutor<Tournament> {
    Optional<Tournament> findByIdAndStatus(Long id, TournamentStatus status);

    Optional<Tournament> findByStatus(TournamentStatus tournamentStatus);

    @Query(value = "SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Tournament t " +
            "WHERE t.status <> 'TERMINATE' " +
            "AND :startDate > (t.end_date + INTERVAL '1 day')",
            nativeQuery = true)
    boolean findByDate(@Param("startDate") Date startDate);
}
