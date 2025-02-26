package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    @Query("SELECT t FROM AccessToken t WHERE t.account.accountId = :accountId AND t.expired = false AND t.revoked = false")
    List<AccessToken> findAllValidTokensByUser(UUID accountId);

    AccessToken findByToken(String AccessToken);
}
