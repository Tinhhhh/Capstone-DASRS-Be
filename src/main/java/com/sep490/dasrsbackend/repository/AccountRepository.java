package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);

    List<Account> findByTeamId(Long id);

    List<Account> findByTeamIdAndIsLocked(Long id, boolean isLocked);
}


