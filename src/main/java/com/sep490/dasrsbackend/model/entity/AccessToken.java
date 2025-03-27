package com.sep490.dasrsbackend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "access_tokens")
public class AccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 1024)
    private String token;

    private boolean expired;

    private boolean revoked;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "refresh_token_id")
    private RefreshToken refreshToken;
}
