package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Record;
import com.sep490.dasrsbackend.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {
    List<Record> findByMatchId(Long matchId);

    List<Record> findByStatusNot(RecordStatus status);
}
