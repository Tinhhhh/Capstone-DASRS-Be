package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long>, JpaSpecificationExecutor<Tournament> {
    Optional<Tournament> findByIdAndStatus(Long id, TournamentStatus status);

}
