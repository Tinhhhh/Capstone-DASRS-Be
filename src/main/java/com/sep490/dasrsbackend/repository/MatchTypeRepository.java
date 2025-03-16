package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.MatchType;
import com.sep490.dasrsbackend.model.enums.MatchTypeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchTypeRepository extends JpaRepository<MatchType, Long> {
    Optional<MatchType> findByIdAndStatus(Long matchTypeId, MatchTypeStatus matchTypeStatus);
}
