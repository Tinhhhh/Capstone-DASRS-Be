package com.sep490.dasrsbackend.security;

import com.sep490.dasrsbackend.model.entity.AccessToken;
import com.sep490.dasrsbackend.model.entity.RefreshToken;
import com.sep490.dasrsbackend.repository.AccessTokenRepository;
import com.sep490.dasrsbackend.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomLogoutHandler.class);
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("text/plain");
            try {
                response.getWriter().write("No JWT token found in the request header");
            } catch (IOException e) {
                logger.error("Error writing unauthorized response", e);
            }

            return;
        }

        //Terminate current accessToken and refreshToken
        jwt = authHeader.substring(7);
        AccessToken storedToken = accessTokenRepository.findByToken(jwt);
        RefreshToken refreshToken = storedToken.getRefreshToken();
        if (refreshToken != null) {
            refreshToken.setExpired(true);
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        }
        storedToken.setRevoked(true);
        storedToken.setExpired(true);
        accessTokenRepository.save(storedToken);
    }
}
