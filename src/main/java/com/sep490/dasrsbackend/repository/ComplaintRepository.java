package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Complaint;
import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByMatchId(Long matchId);

    List<Complaint> findByStatusNot(ComplaintStatus status);

    List<Complaint> findByStatus(ComplaintStatus status);

    List<Complaint> findByMatch_MatchTeamList_Team_Id(Long teamId);
}
