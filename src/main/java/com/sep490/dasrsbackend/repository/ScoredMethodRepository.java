package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.ScoredMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoredMethodRepository extends JpaRepository<ScoredMethod, Long> {
}
