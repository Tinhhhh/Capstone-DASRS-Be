package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

   void deleteByRevokedIsTrue();

    Optional<PasswordResetToken> findByToken(String token);
}
