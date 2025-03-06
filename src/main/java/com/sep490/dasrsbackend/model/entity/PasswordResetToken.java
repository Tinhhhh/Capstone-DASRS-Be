package com.sep490.dasrsbackend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int passwordResetTokenId;

    @Column(name = "reset_password_token", nullable = false, unique = true)
    private String token;

    @Column(name = "expired_at")
    private LocalDateTime expired;

    @Column(name = "is_revoked")
    private boolean revoked;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

}
