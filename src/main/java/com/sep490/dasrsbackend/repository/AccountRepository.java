package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);

    List<Account> findByTeamId(Long id);

    List<Account> findByTeamIdAndIsLocked(Long id, boolean isLocked);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.team t")
    List<Account> findAllPlayers();

    @Query("SELECT a FROM Account a WHERE a.team.teamName = :teamName")
    List<Account> findPlayersByTeamName(@Param("teamName") String teamName);

    @Query("SELECT a FROM Account a WHERE a.role.roleName = :role")
    List<Account> findAccountsByRole(@Param("role") String role);
}


