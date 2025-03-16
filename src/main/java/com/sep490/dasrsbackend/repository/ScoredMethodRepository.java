package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.ScoredMethod;
import com.sep490.dasrsbackend.model.enums.ScoredMethodStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScoredMethodRepository extends JpaRepository<ScoredMethod, Long> {
    Optional<ScoredMethod> findByIdAndStatus(Long scoredMethodId, ScoredMethodStatus active);
}
