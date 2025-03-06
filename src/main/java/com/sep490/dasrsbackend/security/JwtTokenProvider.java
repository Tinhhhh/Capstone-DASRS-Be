package com.sep490.dasrsbackend.security;

import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.repository.AccessTokenRepository;
import com.sep490.dasrsbackend.repository.PasswordResetTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    @Value("${app.jwt.secret-key}")
    private String jwtSecret;

    @Value("${app.jwt-access-expiration-milliseconds}")
    private long jwtAccessExpiration;

    @Value("${app.jwt-refresh-expiration-milliseconds}")
    private long jwtRefreshExpiration;

    private final AccessTokenRepository accessTokenRepository;
    private final PasswordResetTokenRepository resetPasswordTokenRepository;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException e) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Invalid JWT token");
        } catch (ExpiredJwtException e) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Expired JWT token");
        } catch (UnsupportedJwtException e) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "JWT claims string is empty");
        }
    }

//    public String getUsernameFromJwt(String jwt) {
//        Claims claims = Jwts.parserBuilder()
//                .setSigningKey(key())
//                .build()
//                .parseClaimsJws(jwt)
//                .getBody();
//        return claims.getSubject();
//    }

    public String generateAccessToken(Authentication authentication) {
        String token = generateToken(authentication, jwtAccessExpiration);
        return token;
    }

    public String generateRefreshToken(Authentication authentication) {
        String token = generateToken(authentication, jwtRefreshExpiration);
        return token;
    }

    public String generateToken(Authentication authentication, long expiration) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + expiration);
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .claim("role", authentication.getAuthorities())
                .signWith(key())
                .compact();
        return token;
    }

    public String getUsernameFromJwt(String jwt) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        String username = claims.getSubject();
        return username;
    }

    public boolean isTokenValid(String jwt, String username) {
        String tokenUsername = getUsernameFromJwt(jwt);
        return tokenUsername.equals(username) && !isTokenExpired(jwt);
    }

    private boolean isTokenExpired(String jwt) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledDeleteExpiredToken() {
        passwordResetTokenRepository.deleteByRevokedIsTrue();
    }

//    @Scheduled(cron = "0 0 2 * * *")
//    public void scheduledDeleteExpiredResetPasswordToken() {
//        resetPasswordTokenRepository.deleteTokensByRevokedTrue();
//    }
}
