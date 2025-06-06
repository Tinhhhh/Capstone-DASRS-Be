package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Complaint;
import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {

    List<Complaint> findByStatusNot(ComplaintStatus status);

    List<Complaint> findByStatus(ComplaintStatus status);

    List<Complaint> findByMatchTeam_Match_Id(Long matchId);

    Page<Complaint> findByMatchTeam_Team_Id(Long teamId, Specification<Complaint> spec, Pageable pageable);

    @Query("SELECT c FROM Complaint c " +
            "JOIN c.matchTeam mt " +
            "JOIN mt.match m " +
            "WHERE m.round.id = :roundId")
    List<Complaint> findByRoundId(@Param("roundId") Long roundId);

    List<Complaint> findByMatchTeam_Id(Long matchTeamId);

    Optional<Complaint> findComplaintByMatchTeamId(Long matchTeamId);
    Optional<Complaint> findComplaintByMatchId(Long matchId);
}
