package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
}
