package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Environment;
import com.sep490.dasrsbackend.model.enums.EnvironmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
    Optional<Environment> findByIdAndStatus(Long id, EnvironmentStatus status);
}
