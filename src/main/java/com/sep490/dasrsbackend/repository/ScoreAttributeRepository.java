package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.ScoreAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreAttributeRepository extends JpaRepository<ScoreAttribute, Long> {
}
