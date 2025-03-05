package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<Record, Long> {
}
