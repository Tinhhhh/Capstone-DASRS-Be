package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.RaceMap;
import com.sep490.dasrsbackend.model.enums.MapStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RaceMapRepository extends JpaRepository<RaceMap, Long> {
    Optional<RaceMap> findByIdAndStatus(Long mapId, MapStatus mapStatus);
}
