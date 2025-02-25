package com.exe201.ilink.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "reset_password_token", nullable = false, unique = true)
    private String token;

    @Column(name = "expired_at")
    private LocalDateTime expiryDate;

    @Column(name = "is_revoked")
    private boolean isRevoked;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

}
