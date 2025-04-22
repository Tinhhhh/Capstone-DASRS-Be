package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByTeamTag(String teamTag);

    Optional<Team> findTeamByTeamNameAndTeamTag(String teamName, String teamTag);

    int countByCreatedDateBetween(Date start, Date end);
}
